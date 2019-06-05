package com.tn.log.access.executor;

import com.tn.log.access.consts.RequestType;
import com.tn.log.access.annotation.LogAccess;
import com.tn.log.access.jackson.ObjectMapperSingleton;
import com.tn.log.access.type.Tuple;
import com.tn.log.access.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Http请求的访问日志统计执行器
 *
 * @author chenck
 * @date 2019/5/10 14:09
 */
public class HttpServletRequestLogStatExecutor extends AbstarctLogStatExecutor {

    /**
     * 针对Http请求
     */
    private HttpServletRequest request;

    public HttpServletRequestLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args) {
        this(logAccess, targetMethod, args, RequestUtil.getRequest());
    }

    public HttpServletRequestLogStatExecutor(LogAccess logAccess, Method targetMethod, Object[] args, HttpServletRequest request) {
        super(logAccess, targetMethod, args, RequestType.HTTP.getType());
        assert request != null;
        this.request = request;
    }

    @Override
    public void statRequestParam(Object arguments) {
        logAccessStatInfo.setClient_ip(RequestUtil.getRequestIp(request));
        // 统计cookie
        // statRequestCookies();

        String reqText = "";
        // 如果没有输入的参数对象也从query string中获取
        if (arguments == null) {
            reqText = RequestUtil.getRequestString(request);
        } else if (isNativeType(arguments)) {
            reqText = String.valueOf(arguments);
        } else {
            reqText = ObjectMapperSingleton.obj2Json(arguments);
        }
        logAccessStatInfo.setReq_txt(reqText);
    }

    /**
     * 过滤输入参数
     * 从接口调用的函数中获取输入数据，只获取非HttpServlet或Model/ModelAndView的参数
     */
    @Override
    public Object filterArgs(Object[] args) {
        if (args == null || args.length <= 0) {
            return null;
        }

        ArrayList<Object> inputArgs = new ArrayList<Object>(args.length);
        for (Object arg : args) {
            // 过滤掉非输入的object
            if (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)
                    && !(arg instanceof Model) && !(arg instanceof ModelAndView)) {
                inputArgs.add(arg);
            }
        }
        if (inputArgs.size() > 1) {
            return inputArgs.toArray();
        }
        if (inputArgs.size() > 0) {
            return inputArgs.get(0);
        }
        return null;
    }

    /**
     * 记录cookie中的key值
     */
    public void statRequestCookies() {
        if (null == request) {
            return;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return;
        }
        for (Cookie c : cookies) {
            String name = c.getName();
            if (StringUtils.isNotBlank(name)) {
                logAccessStatInfo.setAttachmentValue(name, c.getValue());
            }
        }
    }

    /**
     * 统计模块名和服务名
     * 若@LogAccess未配置模块名和服务名，则从请求URI中获取模块和接口名
     */
    @Override
    public void statModuleAndServiceName() {
        // 只有模块名或接口名未指定时才从对应的请求对象中获取
        Tuple.Tuple2<String, String> modinfo = RequestUtil.getModuleInfo(request, targetMethod);
        if (StringUtils.isBlank(logAccess.module_name())) {
            logAccessStatInfo.setMoudle_name(modinfo._1());
        }
        if (StringUtils.isBlank(logAccess.service_name())) {
            logAccessStatInfo.setService_name(modinfo._2());
        }
    }
}
