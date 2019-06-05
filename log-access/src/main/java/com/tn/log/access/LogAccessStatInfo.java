package com.tn.log.access;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 访问日志统计信息
 *
 * @author chenck
 * @date 2019/5/10 13:37
 */
@Data
public class LogAccessStatInfo {

    public LogAccessStatInfo() {
    }

    public LogAccessStatInfo(String request_type) {
        this.request_type = request_type;
    }

    /**
     * 访问时间
     */
    private long access_time;
    /**
     * 执行耗时ms
     */
    private long exec_time;
    /**
     * 执行结果，用来标记本次执行是正常的响应还是出现了异常
     * N 对应normal，表示正常响应，E 对应error，表示出现异常
     */
    private String exec_result;
    /**
     * 请求类型 普通方法、http请求、dubbo请求
     */
    private String request_type;
    /**
     * 模块名
     */
    private String moudle_name;
    /**
     * 接口名
     */
    private String service_name;
    /**
     * 本机IP
     */
    private String localhost_ip;
    /**
     * 客户端IP
     */
    private String client_ip;
    /**
     * 消息跟踪号
     */
    private String trace_id;
    /**
     * 请求串
     */
    private String req_txt = "";
    /**
     * 返回的结果串
     */
    private String resp_txt = "";
    /**
     * 附件参数，以便扩展
     */
    private Map<String, String> attachtment = new HashMap<String, String>();

    public void setAttachmentValue(String key, String value) {
        if (key != null && value != null) {
            this.attachtment.put(key, value);
        }
    }
}
