package com.tn.log.access.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 访问日志
 *
 * @author chenck
 * @date 2019/5/9 21:20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LogAccess {

    /**
     * 模块名
     */
    public String module_name() default "";

    /**
     * 接口名
     */
    public String service_name() default "";

    /**
     * 是否显示输入参数的集合类型值
     */
    public boolean showInCollection() default true;

    /**
     * 是否显示输入参数的Map类型值
     */
    public boolean showInMap() default true;

    /**
     * 是否显示输入参数的数组类型值
     */
    public boolean showInArray() default true;

    /**
     * 是否显示输出参数的集合类型值
     */
    public boolean showOutCollection() default false;

    /**
     * 是否显示输出参数的Map类型值
     */
    public boolean showOutMap() default false;

    /**
     * 是否显示输出参数的数组类型值
     */
    public boolean showOutArray() default false;
}
