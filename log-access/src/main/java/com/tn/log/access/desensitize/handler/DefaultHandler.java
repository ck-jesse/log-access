package com.tn.log.access.desensitize.handler;

import com.tn.log.access.desensitize.Handler;

/**
 * 默认的数据处理器
 *
 * @author chenck
 * @date 2019/5/10 11:36
 */
public class DefaultHandler implements Handler<Object, Object> {
    @Override
    public Object handler(Object value, String format) {
        return value;
    }
}
