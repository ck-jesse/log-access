package com.tn.log.access.executor;

import com.alibaba.dubbo.rpc.RpcContext;
import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 日志统计执行器工厂
 *
 * @author chenck
 * @date 2019/5/13 11:13
 */
public class LogStatExecutorFactory {

    /**
     * springmvc 暴露的http接口
     * <p>
     * 注：因为存在使用该组件的服务中所依赖的spring-web版本低于4.3，故会存在在使用PostMapping等注解时出现ClassNotFoundException的情况，
     * 所以此处将各个PostMapping等注解的定义，使用类的全路径，比避免这种情况，达到兼容各种spring-web版本的情况
     */
    private static final Set<String> mappingsSet = new HashSet<String>();
    /**
     * 基于dubbo本身的配置接口
     */
    private static final Set<String> dubboServiceSet = new HashSet<String>();

    static {
        mappingsSet.add("org.springframework.web.bind.annotation.Mapping");
        mappingsSet.add("org.springframework.web.bind.annotation.RequestMapping");
        mappingsSet.add("org.springframework.web.bind.annotation.PostMapping");
        mappingsSet.add("org.springframework.web.bind.annotation.GetMapping");
        mappingsSet.add("org.springframework.web.bind.annotation.PutMapping");
        mappingsSet.add("org.springframework.web.bind.annotation.DeleteMapping");
        mappingsSet.add("org.springframework.web.bind.annotation.PatchMapping");

        dubboServiceSet.add("com.alibaba.dubbo.config.annotation.Service");
    }

    /**
     * 获取日志统计执行器
     */
    public static LogStatExecutor getExecutor(LogAccess logAccess, Method targetMethod, Object[] args) {

        // 先判断是否为http请求
        HttpServletRequest request = RequestUtil.getRequest();
        if (isHttpRequest(targetMethod, request)) {
            return new HttpServletRequestLogStatExecutor(logAccess, targetMethod, args, request);
        }

        // 先判断是否为dubbo请求
        RpcContext rpcContext = RpcContext.getContext();
        if (isDubboRequest(targetMethod, rpcContext)) {
            return new DubboRequestLogStatExecutor(logAccess, targetMethod, args, rpcContext);
        }

        // 默认执行器
        return new DefaultRequestLogStatExecutor(logAccess, targetMethod, args);
    }

    /**
     * 判断目标方法是否是基于springmvc实现的http接口
     */
    public static boolean isHttpRequest(Method targetMethod, HttpServletRequest request) {
        if (null == request) {
            return false;
        }
        Annotation[] annotations = targetMethod.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            String annotationType = annotation.annotationType().getName();
            if (mappingsSet.contains(annotationType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断目标方法是否是基于dubbo实现的rpc接口
     * <p>
     * 注意：RpcContext是线程级别的，
     * 场景：http方法A中，先调用了dubbo接口B，再调用dubbo接口C（注：B和C均为远程的dubbo服务）
     * 1、若请求方法A时，且自信完毕后，则RpcContext中包含的是B的相关信息
     * <p>
     * 场景：dubbo接口A中调用本地dubbo接口B
     * 1、当请求dubbo接口A时，RpcContext中始终都是A相关的信息，因为B此时本质就是一个普通的本地方法调用而已
     */
    public static boolean isDubboRequest(Method targetMethod, RpcContext rpcContext) {
        if (null == rpcContext) {
            return false;
        }
        // 当目标方法和RpcContext中的methodName相同时，才看作是dubbo请求
        // 若不相同，则可能是请求dubbo接口A时，A中有调用本地的dubbo接口B，此时本地dubbo接口B的调用看作是本地方法的调用，即普通方法的调用
        if (targetMethod.getName().equals(rpcContext.getMethodName())) {
            return true;
        }
        return false;
    }
}
