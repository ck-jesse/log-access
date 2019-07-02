package com.tn.log.access.filter.servlet;

import com.tn.log.access.filter.TracingVariable;
import com.tn.log.access.util.MDCLogTracerContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * http请求日志跟踪信息Filter
 *
 * @Author chenck
 * @Date 2019/1/9 16:37
 */
public class ServletTraceInfoAttachmentFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ServletTraceInfoAttachmentFilter.class);

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest requestHttp = (HttpServletRequest) request;
        // 默认从请求中获取trace_id（nginx中生成trace_id）
        String traceId = requestHttp.getHeader(TracingVariable.TRACE_ID);
        try {
            // 当请求中无trace_id时，默认生成一个
            if (traceId == null) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDCLogTracerContextUtil.attachTraceId(traceId);
            chain.doFilter(request, response);
        } finally {
            MDCLogTracerContextUtil.removeTraceId();
        }
    }

}
