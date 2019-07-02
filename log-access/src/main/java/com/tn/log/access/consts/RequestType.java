package com.tn.log.access.consts;

/**
 * 请求类型
 *
 * @author chenck
 * @date 2019/5/13 11:05
 */
public enum RequestType {
    METHOD("METHOD", "普通方法的执行"),
    HTTP("HTTP", "http接口的执行"),
    DUBBO("DUBBO", "dubbo接口的执行"),
    OUTAPI("OUTAPI", "调用第三方API"), // 此类型主要是在我方调用第三方API的场景下记录请求参数和响应参数，做到有迹可循，同时方便排查问题
    ;

    private String type;
    private String desc;

    RequestType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
