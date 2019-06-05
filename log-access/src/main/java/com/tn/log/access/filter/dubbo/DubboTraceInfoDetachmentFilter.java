package com.tn.log.access.filter.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.tn.log.access.util.MDCLogTracerContextUtil;
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
        String traceId = RpcContext.getContext().getAttachment("trace_id");
        LOGGER.info("=== DubboTraceInfoDetachmentFilter traceId: {}", traceId);

        if (traceId != null) {
            MDCLogTracerContextUtil.attachTraceId(traceId);
        }
        Result result = null;
        try {
            result = invoker.invoke(invocation);
        } finally {
            MDCLogTracerContextUtil.removeTraceId();
        }
        return result;
    }
}
