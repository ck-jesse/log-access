package com.tn.log.access.aspect;

import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.consts.LogAccessConsts;
import com.tn.log.access.executor.LogStatExecutor;
import com.tn.log.access.executor.LogStatExecutorFactory;
import com.tn.log.access.util.ObjUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * LogAccess 切面
 *
 * @author chenck
 * @date 2019/5/9 21:21
 */
@Component
@Aspect
public class LogAccessAspect implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LogAccessAspect.class);

    private int order = -1000;

    /**
     * 切入点<br>
     *
     * @within：使用“@within(注解类型)”匹配所以持有指定注解类型内的方法；注解类型也必须是全限定类型名；<br>
     * @annotation：使用“@annotation(注解类型)”匹配当前执行方法持有指定注解的方法；注解类型也必须是全限定类型名；<br>
     */
    @Pointcut("@within(com.tn.log.access.annotation.LogAccess) || @annotation(com.tn.log.access.annotation.LogAccess)")
    public void logPointcut() {
        logger.debug("[LogAccess]Log Access point cut.");
    }

    /**
     * 环绕通知：记录接口的访问日志
     */
    @Around(value = "logPointcut()")
    public Object logAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取切点的函数信息
        Method method = null;
        try {
            MethodSignature ms = (MethodSignature) joinPoint.getSignature();
            method = ms.getMethod();
        } catch (Exception e) {
            logger.error("MethodSignature类型转换错误", e);
            throw e;
        }

        // 获取方法上的注解
        LogAccess logAccess = method.getAnnotation(LogAccess.class);
        if (logAccess == null) {
            // 方法上找到不注解则从类上查找注解
            logAccess = method.getDeclaringClass().getAnnotation(LogAccess.class);
        }

        // 不应该出现走到当前方法但目标方法或类上没有定义注解的情况
        if (logAccess == null) {
            return null;
        }

        // 获取方法参数
        Object[] args = joinPoint.getArgs();

        // 打印方法入参信息
        printTraceLogAccessInfo(method, args);

        // 访问日志统计执行器
        LogStatExecutor executor = LogStatExecutorFactory.getExecutor(logAccess, method, args);
        // 启动访问日志统计
        executor.startLogAccessStat();

        Object result = null;
        try {
            // 调用原函数
            result = joinPoint.proceed();

            // 完成访问日志统计
            executor.finishLogAccessStat(result, null);
        } catch (Exception t) {
            // 完成访问日志统计
            executor.finishLogAccessStat(result, t);

            // 捕捉异常处理后直接抛出
            throw t;
        }
        return result;
    }

    /**
     * 打印方法入参信息(可配)
     * 注：日志处理过程中的异常内部消化掉，以免影响业务方法
     */
    private void printTraceLogAccessInfo(Method method, Object[] args) {
        try {
            // 默认不打印
            boolean isPrintArgInfo = false;
            // 查看配置中是否配置需要打印
            String strConf = System.getProperty(LogAccessConsts.PRINT_INPUT_ARGS_ENABLE);
            if (StringUtils.isNotBlank(strConf)) {
                isPrintArgInfo = Boolean.parseBoolean(strConf);
            }

            if (isPrintArgInfo) {
                // 打印类名，函数名，参数值
                logger.info(method.getDeclaringClass().getName() + "." + method.getName() + "|"
                        + ObjUtils.obj2String(args));
            }
        } catch (Throwable t) {
            logger.error("printTraceLogAccessInfo fail, cause ", t);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
