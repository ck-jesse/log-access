package com.tn.log.access.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tn.log.access.consts.LogAccessConsts;
import com.tn.log.access.desensitize.SensitiveDataType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jackjson ObjectMapper的单例
 *
 * @author chenck
 * @date 2019/5/10 15:33
 */
public class ObjectMapperSingleton {

    private static final Logger logger = LoggerFactory.getLogger(ObjectMapperSingleton.class);

    private ObjectMapperSingleton() {
        if (null != InnerClass.objectMapper) {
            throw new IllegalStateException("非法获取ObjectMapperSingleton");
        }
    }

    private static class InnerClass {
        protected static ObjectMapper objectMapper = new ObjectMapper();

        static {
            String maxArraySizeStr = System.getProperty(LogAccessConsts.COLLECTION_MAX_SIZE);
            String maxLengthStr = System.getProperty(LogAccessConsts.STRING_MAX_SIZE);
            int maxArraySize = LogAccessConsts.COLLECTION_MAX_SIZE_DEFAULT;
            int maxLength = LogAccessConsts.STRING_MAX_SIZE_DEFAULT;
            if (StringUtils.isNotBlank(maxArraySizeStr)) {
                maxArraySize = Integer.valueOf(maxArraySizeStr);
            }
            if (StringUtils.isNotBlank(maxLengthStr)) {
                maxLength = Integer.valueOf(maxLengthStr);
            }

            SimpleModule module = new SimpleModule();
            // setSerializers操作必须在addSerializer之前，addSerializer的处理是向serializers的容器中添加
            module.setSerializers(new SimpleCollectionTypeSerializers(maxArraySize));

            SpecStringSerializer strSer = new SpecStringSerializer();
            strSer.setOutTextLen(maxLength);
            strSer.setApplyContext(SensitiveDataType.CT_LOGGING);

            module.addSerializer(strSer);
            module.addSerializer(new MultipartFileSerializer());
            objectMapper.registerModule(module);
        }
    }

    public static ObjectMapper getInstance() {
        return InnerClass.objectMapper;
    }


    /**
     * 对象转json格式字符串
     */
    public static String obj2Json(Object obj) {
        if (null == obj) {
            return "";
        }
        try {
            return getInstance().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            //吞噬异常，打印错误日志,出现问题只是日志记录有问题，不能影响正常的业务
            logger.error("arlog obj2Json failed", e);
        }
        return null;
    }

}
