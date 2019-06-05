package com.tn.log.access.desensitize;

/**
 * 敏感数据类型
 *
 * @author chenck
 * @date 2019/5/10 11:19
 */
public interface SensitiveDataType {

    /**
     * 敏感数据为手机号
     */
    public final static int DT_MOBILE = 1;

    /**
     * 敏感数据为身份证号
     */
    public final static int DT_IDNO = 2;

    /**
     * 敏感数据为密码
     */
    public final static int DT_PASSWORD = 3;

    /**
     * 敏感数据为卡号
     */
    public final static int DT_CARDNO = 4;

    /**
     * 敏感数据为应用的密钥
     */
    public final static int DT_APPSECRET = 5;

    /**
     * 敏感数据为用户名
     */
    public final static int DT_USERNAME = 6;

    /**
     * 敏感数据为email
     */
    public final static int DT_EMAIL = 7;

    /**
     * 特定的类型，需要指定formatter处理
     */
    public final static int DT_SPECIFIC = 9999;

    /**
     * ============================
     * 敏感数据上下文类型
     */
    /**
     * 日志打印
     */
    public final static int CT_LOGGING = 1;

    /**
     * 所有场景
     */
    public final static int CT_ALL = 99999;
}
