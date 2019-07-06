package com.tn.log.access.executor;

import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.consts.RequestType;
import com.tn.log.access.jackson.ObjectMapperSingleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.Method;

/**
 * dubbo请求的日志统计执行器
 *
 * @author chenck
 * @date 2019/5/10 14:09
 */
public class DubboRequestLogStatExecutor extends AbstarctLogStatExecutor {

    /**
     * 针对dubbo请求
     */
    private RpcContext rpcContext;

    public DubboRequestLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args) {
        this(logAccess, targetMethod, args, RpcContext.getContext());
    }

    public DubboRequestLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args, RpcContext rpcContext) {
        super(logAccess, targetMethod, args, RequestType.DUBBO.getType());
        assert rpcContext != null;
        this.rpcContext = rpcContext;
    }

    @Override
    public void statRequestParam(Object arguments) {
        logAccessStatInfo.setClient_ip(rpcContext.getRemoteAddressString());
        String reqText = ObjectMapperSingleton.obj2Json(arguments);
        logAccessStatInfo.setReq_txt(reqText);
    }

    @Override
    public void statModuleAndServiceName() {
        if (StringUtils.isBlank(logAccess.module_name())) {
            logAccessStatInfo.setMoudle_name(rpcContext.getUrl().toServiceString());
        }
        if (StringUtils.isBlank(logAccess.service_name())) {
            logAccessStatInfo.setService_name(targetMethod.getName());
        }
    }
}
