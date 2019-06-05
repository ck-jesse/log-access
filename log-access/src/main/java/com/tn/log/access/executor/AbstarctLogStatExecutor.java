package com.tn.log.access.executor;

import com.tn.log.access.AccessLogger;
import com.tn.log.access.LogAccessStatInfo;
import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.consts.LogAccessConsts;
import com.tn.log.access.exception.BizException;
import com.tn.log.access.jackson.ObjectMapperSingleton;
import com.tn.log.access.util.NetUtils;
import com.tn.log.access.util.MDCLogTracerContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 抽象的日志统计执行器
 *
 * @author chenck
 * @date 2019/5/13 10:24
 */
public abstract class AbstarctLogStatExecutor implements LogStatExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AbstarctLogStatExecutor.class);

    protected LogAccess logAccess;
    /**
     * 拦截的目标方法
     */
    protected Method targetMethod;
    /**
     * 拦截的目标方法的参数
     */
    protected Object[] args;
    /**
     * 访问日志统计信息对象
     */
    protected LogAccessStatInfo logAccessStatInfo;

    protected AbstarctLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args, String requestType) {
        this.logAccess = logAccess;
        this.targetMethod = targetMethod;
        this.args = args;
        this.logAccessStatInfo = new LogAccessStatInfo(requestType);
    }

    @Override
    public void startLogAccessStat() {
        try {
            long startTime = System.currentTimeMillis();

            logAccessStatInfo.setAccess_time(startTime);
            logAccessStatInfo.setTrace_id(MDCLogTracerContextUtil.getTraceId());
            logAccessStatInfo.setLocalhost_ip(NetUtils.getLocalHostIP());
            logAccessStatInfo.setMoudle_name(logAccess.module_name());
            logAccessStatInfo.setService_name(logAccess.service_name());

            if (StringUtils.isBlank(logAccess.module_name()) || StringUtils.isBlank(logAccess.service_name())) {
                // 统计模块名和服务名
                statModuleAndServiceName();
            }

            // 参数过滤
            Object arguments = filterArgs(args);

            // 统计请求参数
            statRequestParam(arguments);

            // 记录统计接口自身耗时
            long methodUsedTime = System.currentTimeMillis() - startTime;
            logger.debug("startLogAccessStat method use time(ms):" + methodUsedTime);
        } catch (Throwable t) {
            logger.error("startLogAccessStat fail, cause ", t);
        }
    }

    @Override
    public void finishLogAccessStat(Object result, Exception throwable) {
        try {
            long startTime = System.currentTimeMillis();
            if (throwable != null) {
                logAccessStatInfo.setExec_result(LogAccessConsts.EXEC_RESULT_ERROR);
                // 如果异常是框架的BizException，则从中获取错误码信息
                if (throwable instanceof BizException) {
                    BizException bizException = (BizException) throwable;
                    logAccessStatInfo.setResp_txt("retcode=" + bizException.getCode() + "&retmsg=" + bizException.getMsg());
                } else {
                    Object code = BizException.ERROR;
                    Object msg = throwable.getMessage();
                    Class<?> clazz = throwable.getClass();
                    try {
                        // 非框架的自定义异常，从异常类中获取code和msg字段对应的get方法，若不存在，则看作其他异常
                        Method codeMethod = clazz.getMethod("getCode");
                        if (null != codeMethod) {
                            code = codeMethod.invoke(throwable);

                            Method msgMethod = clazz.getMethod("getMsg");
                            if (null != msgMethod) {
                                msg = msgMethod.invoke(throwable);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        try {
                            Method msgMethod = clazz.getMethod("getMessage");
                            if (null != msgMethod) {
                                msg = msgMethod.invoke(throwable);
                            }
                        } catch (Exception e1) {
                            logger.error("finishLogAccessStat get code and message error from Exception:" + throwable.getClass() + ", cause ", e1);
                        }
                    } catch (Exception e) {
                        logger.error("finishLogAccessStat get code and msg error from Exception:" + throwable.getClass() + ", cause ", e);
                    }
                    if (null == code) {
                        code = BizException.ERROR;
                    }
                    if (null == msg) {
                        msg = throwable.getMessage();
                    }

                    // 其他异常
                    StringBuilder sb = new StringBuilder(128);
                    sb.append("retcode=");
                    sb.append(code);
                    sb.append("&retmsg=");
                    sb.append(throwable.getClass().toString());
                    sb.append("[");
                    sb.append(msg);
                    sb.append("]");
                    logAccessStatInfo.setResp_txt(sb.toString());
                }
            } else {
                logAccessStatInfo.setExec_result(LogAccessConsts.EXEC_RESULT_NORMAL);
                if (null != result) {
                    if (isNativeType(result)) {
                        logAccessStatInfo.setResp_txt(result.toString());
                    } else {
                        logAccessStatInfo.setResp_txt(ObjectMapperSingleton.obj2Json(result));
                    }
                } else {
                    logger.debug("finishLogAccessStat result is null");
                }
            }
            // 接口耗时
            logAccessStatInfo.setExec_time(System.currentTimeMillis() - logAccessStatInfo.getAccess_time());
            logger.debug("finishLogAccessStat checkpoint1 use time(ms):" + (System.currentTimeMillis() - startTime));

            // 将接口统计信息打印的特定的logger中
            AccessLogger.log(logAccessStatInfo);

            // 记录统计接口自身耗时
            long methodUsedTime = System.currentTimeMillis() - startTime;
            logger.debug("finishLogAccessStat method use time(ms):" + methodUsedTime);
        } catch (Throwable t) {
            logger.error("finishLogAccessStat fail, cause ", t);
        }
    }

    /**
     * 过滤输入参数
     */
    protected Object filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        if (args.length > 1) {
            return args;
        }
        if (args.length == 1) {
            return args[0];
        }
        return null;
    }

    /**
     * 检查参数是否为原始数据类型(不含集合和Map的检查)
     *
     * @param arg
     * @return
     */
    protected boolean isNativeType(Object arg) {
        if (arg instanceof Number || arg instanceof CharSequence || arg instanceof Character || arg instanceof Boolean) {
            return true;
        }
        return false;
    }

    /**
     * 统计模块名和服务名
     * 若@LogAccess未配置模块名和服务名，则从请求URI中获取模块和接口名
     */
    public abstract void statModuleAndServiceName();

    /**
     * 统计请求参数
     */
    public abstract void statRequestParam(Object arguments);
}
