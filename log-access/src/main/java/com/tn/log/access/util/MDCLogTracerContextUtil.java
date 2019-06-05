package com.tn.log.access.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 基于slf4j的MDC的日志跟踪
 *
 * @author chenck
 * @date 2019/5/11 15:34
 */
public class MDCLogTracerContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(MDCLogTracerContextUtil.class);

    static String localIp;

    public static void attachTraceId(String traceId) {
        if (traceId == null) {
            logger.error("traceId can not be null");
            return;
        }

        mdcPut("trace_id", traceId);
        mdcPut("local_ip", localIp);
    }

    private static void mdcPut(String key, String val) {
        MDC.put(key, val);
    }

    private static void mdcRemove(String key) {
        MDC.remove(key);
    }

    private static String mdcGet(String key) {
        return MDC.get(key);
    }

    public static void removeTraceId() {
        mdcRemove("trace_id");
    }

    public static String getTraceId() {
        return mdcGet("trace_id");
    }

    public static void main(String[] args) {
        attachTraceId("xx");
    }

    static {
        localIp = "";
        try {
            localIp = NetUtils.getLocalHostIP();
        } catch (Exception e) {
            logger.error("fetch local ip error", e);
        }
    }

}
