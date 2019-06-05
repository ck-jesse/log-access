package com.tn.log.access.desensitize;

/**
 * 数据脱敏处理器
 *
 * @author chenck
 * @date 2019/5/10 11:28
 */
public interface Handler<IN, OUT> {

    /**
     * 对value参数按fmt指定的参数进行格式化
     *
     * @param value
     * @param format
     * @return
     */
    public OUT handler(IN value, String format);

}
