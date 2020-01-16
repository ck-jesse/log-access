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
 * 附加trace_id的过滤器
 *
 * @author chenck
 * @date 2019/5/11 16:18
 */
@Activate(group = {"consumer"})
public class DubboTraceInfoAttachmentFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboTraceInfoAttachmentFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = MDCLogTracerContextUtil.getTraceId();
        LOGGER.debug("Attachment traceId = {}", traceId);

        if (traceId != null) {
            RpcContext.getContext().setAttachment(TracingVariable.TRACE_ID, traceId);
        }
        return invoker.invoke(invocation);
    }
}
