package com.tn.log.access;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 访问日志记录器
 *
 * @author chenck
 * @date 2019/5/10 18:23
 */
public class AccessLogger {

    /**
     * 对应logback.xml中元素 <logger name="access_logger" > 的 name 属性（必须保持一致）
     */
    private static final String WATCH_LOGGER_NAME = "access_logger";

    /**
     * 保存access日志
     */
    private static final Logger accessLogger = LoggerFactory.getLogger(WATCH_LOGGER_NAME);

    /**
     * 日志打印
     */
    private static final Logger logger = LoggerFactory.getLogger(AccessLogger.class);

    /**
     * 记录接口访问日志
     *
     * @param stat
     */
    public static void log(LogAccessStatInfo stat) {
        try {
            StringBuilder sb = new StringBuilder(1024);
            sb.append(getDateFromMillis(stat.getAccess_time()));
            sb.append("|");
            sb.append(stat.getExec_result());
            sb.append("|");
            sb.append(stat.getRequest_type());
            sb.append("|");
            sb.append(stat.getExec_time()).append("ms");
            sb.append("|");
            sb.append(stat.getMoudle_name());
            sb.append("|");
            sb.append(stat.getService_name());
            sb.append("|");
            sb.append(stat.getLocalhost_ip());
            sb.append("|");
            sb.append(stat.getClient_ip());
            sb.append("|");
            sb.append(fixField(stat.getTrace_id(), 64));
            sb.append("|");
            sb.append(fixField(stat.getReq_txt(), 10200));
            sb.append("|");
            sb.append(fixField(stat.getResp_txt(), 10200));
            sb.append("|attachtment|");
            sb.append(fixField(map2String(stat.getAttachtment()), 4080));

            accessLogger.info(sb.toString());
        } catch (Throwable e) {
            logger.error("写入access日志失败", e);
        }
    }

    /**
     * 将毫秒转换为日期yyyy-MM-dd HH:mm:ss
     *
     * @param millis
     * @return
     */
    public static String getDateFromMillis(long millis) {
        Date nowTime = new Date(millis);
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdFormatter.format(nowTime);
    }


    /**
     * 对输出的访问日志进行长度和特殊字符（|、CR、NL）处理
     *
     * @param s
     * @param maxlen
     * @return
     */
    private static String fixField(String s, int maxlen) {
        if (s == null || s.length() == 0) {
            return "";
        }
        String str = s;
        if (s.length() > maxlen) {
            str = s.substring(0, maxlen);
        }
        // 替换|、CR、NL字符 ，让打印的日志不换行
        return org.apache.commons.lang3.StringUtils.replaceEachRepeatedly(str,
                new String[]{"|", "\r", "\n"}, new String[]{"%7C", "%0D", "%0A"});
    }

    /**
     * 将map中的数据转主k=v&k=v字串
     *
     * @param map
     * @return
     */
    public static String map2String(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        // 是否首次拼接
        boolean bfirst = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!bfirst) {
                sb.append("&");
            } else {
                bfirst = false;
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        return sb.toString();
    }
}
