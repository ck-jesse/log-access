package com.tn.log.access.exception;

/**
 * @author chenck
 * @date 2019/5/11 16:51
 */
public class BizException extends RuntimeException {

    public static final String SUCC = "200";
    public static final String ERROR = "500";

    protected String code = SUCC;
    protected String msg;

    public BizException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BizException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
