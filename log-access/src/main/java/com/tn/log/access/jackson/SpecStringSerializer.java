package com.tn.log.access.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.tn.log.access.desensitize.Handler;
import com.tn.log.access.desensitize.SensitiveData;
import com.tn.log.access.desensitize.SensitiveDataHandlerFactory;
import com.tn.log.access.desensitize.SensitiveDataType;
import com.tn.log.access.desensitize.handler.DefaultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感数据序列化，根据场景和数据类型来进行敏感数据的脱敏
 * 比如手机号隐藏中间4位，身份证号隐藏中间部分
 *
 * @author chenck
 * @date 2019/5/10 12:01
 */
public class SpecStringSerializer extends StdScalarSerializer<String> implements
        ContextualSerializer {
    private static final Logger logger = LoggerFactory.getLogger(SpecStringSerializer.class);

    protected final static ConcurrentHashMap<Class<? extends Handler>, Handler<String, String>> handlers = new ConcurrentHashMap<Class<? extends Handler>, Handler<String, String>>();

    private static final long serialVersionUID = 1L;

    /**
     * 敏感数据处理的上文环境
     */
    private int applyContext;

    /**
     * 属性上的使用的敏感信息注解值
     */
    private SensitiveData sensitiveAnn;

    /**
     * 输出的长度,默认值0表示不限制长度按原值输出
     */
    private int outTextLen = 0;

    public int getOutTextLen() {
        return outTextLen;
    }

    public void setOutTextLen(int outTextLen) {
        this.outTextLen = outTextLen;
    }

    public int getApplyContext() {
        return applyContext;
    }

    public void setApplyContext(int applyContext) {
        this.applyContext = applyContext;
    }

    public SensitiveData getSensitiveAnn() {
        return sensitiveAnn;
    }

    public void setSensitiveAnn(SensitiveData sensitiveAnn) {
        this.sensitiveAnn = sensitiveAnn;
    }

    public SpecStringSerializer() {
        super(String.class, false);
        this.applyContext = SensitiveDataType.CT_ALL;
    }

    public SpecStringSerializer(SensitiveData sensitiveAnn, int applyContext) {
        super(String.class, false);

        this.sensitiveAnn = sensitiveAnn;
        this.applyContext = applyContext;

    }

    @Override
    public void serialize(final String s, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        String handledData = s;
        //如果没有使用注解则按正常值输出
        if (sensitiveAnn != null) {
            //检查敏感过滤的场景,false不需要过滤
            boolean dealSensitive = checkApplyContext(sensitiveAnn.context());

            //如果不需要过滤则直接输出
            if (dealSensitive) {
                //进行脱敏处理
                handledData = hideData(s);
            }
        }

        //判断是否超过最大输出值，超过之前截断，在最后输出...+内容长度
        if (outTextLen > 0 && handledData != null && handledData.length() > outTextLen) {
            StringBuilder sb = new StringBuilder(outTextLen + 10);
            sb.append(handledData.substring(0, outTextLen));
            sb.append(" #Len:");
            sb.append(handledData.length());
            jsonGenerator.writeString(sb.toString());
        } else {
            jsonGenerator.writeString(handledData);
        }
    }

    /**
     * 进行数据脱敏处理
     *
     * @param value
     * @return
     */
    protected String hideData(final String value) {
        Class<? extends Handler> handlerClass = sensitiveAnn.handler();
        Handler<String, String> fmt = null;
        // 如果handler是默认值，则按type获取对应的handler
        if (handlerClass.equals(DefaultHandler.class)) {
            fmt = SensitiveDataHandlerFactory.getHandler(sensitiveAnn.type());
        } else {
            // 如果指定了handler，则先查看handler是否已实例化，未实例化则先实例化
            fmt = handlers.get(handlerClass);
            if (fmt == null) {
                synchronized (handlers) {
                    fmt = handlers.get(handlerClass);
                    if (fmt == null) {
                        // 实例化handler
                        fmt = createFormatterInstance(handlerClass);
                        // 添加到handler容器中
                        handlers.put(handlerClass, fmt);
                    }
                }
            }
        }
        if (fmt == null) {
            return value;
        }
        return fmt.handler(value, sensitiveAnn.format());
    }

    /**
     * 创建一个Handler
     *
     * @param handlerClass
     * @return
     */
    protected Handler<String, String> createFormatterInstance(Class<? extends Handler> handlerClass) {
        try {
            Handler<String, String> handler = handlerClass.newInstance();
            return handler;
        } catch (Exception e) {
            logger.error("create sensitive handler fail", e);
        }
        return null;
    }

    /**
     * 检查敏感信息的处理场景
     *
     * @param contexts
     * @return false-不需要脱敏处理；true-进行脱敏处理
     */
    protected boolean checkApplyContext(int[] contexts) {
        if (contexts == null || contexts.length == 0) {
            return false;
        }

        //如果注册的序列化支持的场景是ALL，则直接返回需要脱敏处理
        if (SensitiveDataType.CT_ALL == this.applyContext) {
            return true;
        }

        for (int ct : contexts) {
            //如果注解中配置应用到所有的场景，则直接返回需要脱敏处理
            if (ct == SensitiveDataType.CT_ALL) {
                return true;
            }

            //如果注解中配置的应用场景匹配上了注册的序列化支持的场景，则直接返回需要脱敏处理
            if (ct == this.applyContext) {
                return true;
            }
        }

        return false;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty == null) {
            return new StringSerializer();
        }

        //只处理的bean属性中有注解的值
        SensitiveData sensitiveAnn = beanProperty.getAnnotation(SensitiveData.class);
        if (sensitiveAnn == null) {
            sensitiveAnn = beanProperty.getContextAnnotation(SensitiveData.class);
        }

        SpecStringSerializer ser = new SpecStringSerializer(sensitiveAnn, this.applyContext);
        ser.setOutTextLen(this.outTextLen);

        return ser;
    }

    //以下为StringSerializer的源码实现，复过来主要是用此Serializer来代替StringSerializer
    //StringSerializer为final的class，无法继承

    @Override
    public boolean isEmpty(SerializerProvider prov, String value) {
        String str = (String) value;
        return str.length() == 0;
    }

    @Override
    public final void serializeWithType(String value, JsonGenerator gen, SerializerProvider provider,
                                        TypeSerializer typeSer) throws IOException {
        // no type info, just regular serialization
        serialize(value, gen, provider);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("string", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitStringFormat(visitor, typeHint);
    }
}
