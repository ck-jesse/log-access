package com.tn.log.access.filter;

import com.tn.log.access.util.MDCLogTracerContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 针对 RestTempalte 进行扩展，将trace_id添加到请求header中，以便传递到服务提供方，用于服务间的链路追踪。
 * <p>
 * 使用示例：
 * RestTemplate restTemplate = new RestTemplate();
 * restTemplate.setInterceptors(Collections.singletonList(new RestTemplateTraceIdInterceptor()));
 * </p>
 *
 * @author chenck
 * @date 2020/3/27 9:16
 */
public class RestTemplateTraceIdInterceptor implements ClientHttpRequestInterceptor {

    private static Logger logger = LoggerFactory.getLogger(RestTemplateTraceIdInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add(TracingVariable.TRACE_ID, MDCLogTracerContextUtil.getTraceId());
        return execution.execute(request, body);
    }
}