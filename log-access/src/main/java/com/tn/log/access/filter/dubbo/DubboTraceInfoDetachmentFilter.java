package com.tn.log.access.filter.dubbo;

import com.tn.log.access.filter.TracingVariable;
import com.tn.log.access.util.MDCLogTracerContextUtil;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分离trace_id的过滤器
 *
 * @author chenck
 * @date 2019/5/11 16:21
 */
@Activate(group = {"provider"})
public class DubboTraceInfoDetachmentFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboTraceInfoDetachmentFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = RpcContext.getContext().getAttachment(TracingVariable.TRACE_ID);
        LOGGER.info("Detachment traceId = {}", traceId);

        if (traceId != null) {
            MDCLogTracerContextUtil.attachTraceId(traceId);
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            MDCLogTracerContextUtil.removeTraceId();
        }
    }
}
