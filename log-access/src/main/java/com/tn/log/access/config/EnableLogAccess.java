package com.tn.log.access.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启LogAccess组件
 *
 * @author chenck
 * @date 2019/5/14 14:36
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(LogAccessConfiguration.class)
@Configuration
public @interface EnableLogAccess {

    /**
     * 排序编号
     */
    int order() default -1000;
}
