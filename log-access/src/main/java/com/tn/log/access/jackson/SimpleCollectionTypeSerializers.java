package com.tn.log.access.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * 集合的序列化处理器，用于注册到ObjectMapper中，在根据类型获取集合的序例化处理类时直返回
 *
 * @author chenck
 * @date 2019/5/10 11:25
 */
public class SimpleCollectionTypeSerializers extends SimpleSerializers {
    /**
     * 序列化时只序列化集中最前面maxSize部分的元素
     */
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

    public SimpleCollectionTypeSerializers() {
        //pass
    }

    public SimpleCollectionTypeSerializers(int maxSize) {
        if (maxSize >= 0) {
            this.maxSize = maxSize;
        }
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription beanDesc,
                                                      TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        SimpleCollectionSerializer ser = new SimpleCollectionSerializer(type.getContentType(), false, elementTypeSerializer, elementValueSerializer);
        ser.setMaxSize(this.maxSize);
        return ser;
    }

    @Override
    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BeanDescription beanDesc,
                                                          TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        SimpleCollectionSerializer ser = new SimpleCollectionSerializer(type.getContentType(), false, elementTypeSerializer, elementValueSerializer);
        ser.setMaxSize(this.maxSize);
        return ser;
    }
}