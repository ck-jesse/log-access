package com.tn.log.access.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * 集合json序列化。用于集合元素过多，在特定场景下只输出部分元素。比如在接口日志中不需要输出全部元素
 *
 * @author chenck
 * @date 2019/5/11 11:12
 */
public class SimpleCollectionSerializer extends CollectionSerializer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCollectionSerializer.class);

    private int maxSize = 1;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        if (maxSize >= 0) {
            this.maxSize = maxSize;
        } else {
            this.maxSize = 1;
        }
    }

    public SimpleCollectionSerializer() {
        super(null, false, null, null);
    }

    public SimpleCollectionSerializer(JavaType et) {
        super(et, false, null, null);
    }

    public SimpleCollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts,
                                      JsonSerializer<Object> valueSerializer) {
        super(elemType, staticTyping, vts, valueSerializer);
    }

    public SimpleCollectionSerializer(SimpleCollectionSerializer src, BeanProperty property,
                                      TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
        setMaxSize(src.getMaxSize());
    }

    @Override
    public SimpleCollectionSerializer withResolved(BeanProperty property, TypeSerializer vts,
                                                   JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new SimpleCollectionSerializer(this, property, vts, elementSerializer, unwrapSingle);
    }

    @Override
    public void serializeContents(Collection<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        // 如果为null，按jackson原有方式处理
        if (value == null) {
            super.serializeContents(value, g, provider);
            return;
        }

        // 当等于0时不显示元素
        if (maxSize > 0) {
            Collection serValue = null;
            // 如果超过最大的显示数量，则直显示最大的数量
            if (value.size() > maxSize) {
                try {
                    // 新建一个列表，将元素复制过来
                    serValue = value.getClass().newInstance();
                    int copySize = 0;
                    Iterator ite = value.iterator();
                    while (ite.hasNext() && copySize < maxSize) {
                        serValue.add(ite.next());
                        ++copySize;
                    }
                } catch (Exception e) {
                    logger.error("copy serialize collection contents  fail", e);
                }
            } else {
                serValue = value;
            }
            super.serializeContents(serValue, g, provider);
        }

        // 将集合的元素大小显示出来，__c_size：__表示内部,c表示collection
        g.writeStartObject();
        g.writeNumberField("__c_size", value.size());
        g.writeEndObject();
    }
}