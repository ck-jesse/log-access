package com.tn.log.access.executor;

import com.tn.log.access.consts.RequestType;
import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.jackson.ObjectMapperSingleton;
import com.tn.log.access.util.NetUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * 默认的日志统计执行器（针对普通方法的执行进行日志处理）
 *
 * @author chenck
 * @date 2019/5/10 14:09
 */
public class DefaultRequestLogStatExecutor extends AbstarctLogStatExecutor {

    public DefaultRequestLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args) {
        super(logAccess, targetMethod, args, RequestType.METHOD.getType());
    }

    @Override
    public void statRequestParam(Object arguments) {
        // 普通方法执行：将client_ip设置为本机IP
        logAccessStatInfo.setClient_ip(NetUtils.getLocalHostIP());
        String reqText = ObjectMapperSingleton.obj2Json(arguments);
        logAccessStatInfo.setReq_txt(reqText);
    }

    @Override
    public void statModuleAndServiceName() {
        if (StringUtils.isBlank(logAccess.module_name())) {
            logAccessStatInfo.setMoudle_name(targetMethod.getDeclaringClass().getName());
        }
        if (StringUtils.isBlank(logAccess.service_name())) {
            logAccessStatInfo.setService_name(targetMethod.getName());
        }
    }
}
