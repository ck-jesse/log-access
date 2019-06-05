package com.tn.log.access.desensitize;

import com.tn.log.access.desensitize.handler.DefaultHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感数据脱敏处理的注解
 *
 * @author chenck
 * @date 2019/5/10 11:19
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SensitiveData {

    /**
     * 数据类型，不同类型隐藏的信息不一样
     *
     * @return
     */
    public int type();

    /**
     * 上下文场景，用于控制不同场景不同的方式,只在指定的场景中进行脱敏处理
     *
     * @return
     */
    public int[] context() default {SensitiveDataType.CT_ALL};

    /**
     *
     */
    public Class<? extends Handler> handler() default DefaultHandler.class;

    /**
     *
     */
    public String format() default "";

}
