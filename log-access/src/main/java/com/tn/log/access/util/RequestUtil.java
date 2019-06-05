package com.tn.log.access.util;

import com.tn.log.access.type.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * HttpServletRequest的工具类
 *
 * @author chenck
 * @date 2019/5/11 14:03
 */
public class RequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    /**
     * 基于Spring的RequestContextHolder获取HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
        if (reqAttr != null && reqAttr instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) reqAttr).getRequest();
        }
        return null;
    }

    /**
     * 判断是否为远程rpc调用
     */
    public static boolean isRpcCall(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // 如果是dubbo服务则认为是rpc调用

        // 如果是hessian请求则认为是rpc调用
        if (isHessianRequest(request)) {
            return true;
        }

        // 如果是soap的web service请求则认为是rpc调用
        if (isSoapRequest(request)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为soap调用
     *
     * @param request
     * @return
     */
    private static boolean isSoapRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }

        // soap1.2协议中，ContentType为application/soap+xml
        if (contentType.contains("soap")) {
            return true;
        }

        // soap协议中有一个请求头SOAPAction的项，该项可能为空串 因此只判断是否为null
        if (request.getHeader("SOAPAction") != null) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为hessian调用
     *
     * @param request
     * @return
     */
    private static boolean isHessianRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }

        // Hessian协议的ContentType为x-application/hessian
        if (contentType.contains("hessian")) {
            return true;
        }
        return false;
    }

    private static String getFirstValidIp(String ipList) {
        if (ipList == null || ipList.length() == 0) {
            return null;
        }

        String[] ips = ipList.split(",");
        for (String ip : ips) {
            if (StringUtils.isBlank(ip)) {
                continue;
            }
            if ("unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            return ip;
        }
        return null;
    }

    public static String getRequestIp() {
        return getRequestIp(RequestUtil.getRequest());
    }

    /**
     * 获取请求的IP
     */
    public static String getRequestIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = getFirstValidIp(request.getHeader("X-Forwarded-For"));
        if (ip == null) {
            ip = getFirstValidIp(request.getHeader("X-FORWARDED-FOR"));
        }
        if (ip == null) {
            ip = getFirstValidIp(request.getHeader("Proxy-Client-IP"));
        }

        if (ip == null) {
            ip = getFirstValidIp(request.getHeader("WL-Proxy-Client-IP"));
        }
        if (ip == null) {
            ip = getFirstValidIp(request.getHeader("HTTP_CLIENT_IP"));
        }
        if (ip == null) {
            ip = getFirstValidIp(request.getHeader("HTTP_X_FORWARDED_FOR"));
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        if (ip == null) {
            return null;
        }
        return ip.trim().equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }

    /**
     * 获取原始的http请求的内容，主要用于获取web接口中请求内容
     *
     * @param request
     * @return
     */
    public static String getRequestString(HttpServletRequest request) {
        if (request == null) {
            return StrUtils.EMPTY;
        }
        // 如果是rpc调用，则不获取请求内容，rpc调用请求的内容是特定格式
        if (RequestUtil.isRpcCall(request)) {
            return StrUtils.EMPTY;
        }

        String method = request.getMethod();
        if (method == null) {
            method = StrUtils.EMPTY;
        }

        // 是GET方法则从query string中获取
        if (method.equalsIgnoreCase("GET")) {
            return request.getQueryString();
        }

        // 如果是post方法则从请求的body中获取,但需要区分文件上传的 情况
        if (method.equalsIgnoreCase("POST")) {
            try {
                // 先从ParameterMap中获取参数
                String paramterMapStr = RequestUtil.getParameterMapString(request);
                if (StringUtils.isNotBlank(paramterMapStr)) {
                    return paramterMapStr;
                }

                if (RequestUtil.isBinayBodyData(request)) {
                    logger.debug("http data is binay data, ignore fetch for @LogAccess");
                    return "";
                }

                // 直接提取body中的数据
                return getRequestInputStream(request);
            } catch (Throwable t) {
                logger.error("get post data body from request input stream fail", t);
            }
        }
        return StrUtils.EMPTY;
    }


    /**
     * 获取request input stream的数据
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static String getRequestInputStream(HttpServletRequest request) throws Exception {
        // 直接提取body中的数据
        ServletInputStream inputStream = request.getInputStream();
        // 如果不支持重复提取stream则日志打印不再从stream中获取数据
        // 以防止获取之后业务不能再获取到数据
        if (inputStream == null || !inputStream.markSupported()) {
            return StrUtils.EMPTY;
        }

        int length = request.getContentLength();
        if (length <= 0) {
            return null;
        }

        byte[] body = StreamUtils.copyToByteArray(inputStream);
        inputStream.close();
        if (body == null || body.length == 0) {
            return null;
        }
        return new String(body, 0, body.length);
    }

    /**
     * 检查http请求是否是请求的传入的二进制数据，对于octet-stream，image，multipart文件 都认为是二进制的
     *
     * @param request
     * @return
     */
    public static boolean isBinayBodyData(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        contentType = contentType.toLowerCase();

        // 判断Content-Type是否指定为流数据
        if (contentType.contains("stream")) {
            return true;
        }

        // 判断Content-Type是否指定为文件上传
        if (contentType.contains("multipart")) {
            return true;
        }

        // 判断Content-Type是否指定为图片
        if (contentType.contains("image")) {
            return true;
        }
        return false;
    }

    /**
     * 从Request中的ParameterMap中获取参数
     */
    public static String getParameterMapString(HttpServletRequest request) {
        if (request == null) {
            return StrUtils.EMPTY;
        }

        Map<String, String[]> map = request.getParameterMap();

        if (map == null || map.size() <= 0) {
            return StrUtils.EMPTY;
        }

        // 是否首次拼接
        boolean bfirst = true;
        StringBuilder sb = new StringBuilder(100);
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            for (String item : entry.getValue()) {
                if (!bfirst) {
                    sb.append("&");
                } else {
                    bfirst = false;
                }
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(item);
            }
        }
        return sb.toString();
    }

    /**
     * 获取请求url的应用映射部分url
     * 例如 demo-test-war/test/index.html,获得结果为/test/index.html
     *
     * @param request
     * @return
     */
    public static String getReqAppPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        return getRemainingPath(uri, contextPath, true);
    }

    /**
     * 去除context path
     *
     * @param requestUri
     * @param mapping
     * @param ignoreCase
     * @return
     */
    protected static String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
        int index1 = 0;
        int index2 = 0;
        for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
            char c1 = requestUri.charAt(index1);
            char c2 = mapping.charAt(index2);
            if (c1 == ';') {
                index1 = requestUri.indexOf('/', index1);
                if (index1 == -1) {
                    return null;
                }
                c1 = requestUri.charAt(index1);
            }
            if (c1 == c2) {
                continue;
            } else if (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2))) {
                continue;
            }
            return null;
        }
        if (index2 != mapping.length()) {
            return null;
        } else if (index1 == requestUri.length()) {
            return "";
        } else if (requestUri.charAt(index1) == ';') {
            index1 = requestUri.indexOf('/', index1);
        }
        return (index1 != -1 ? requestUri.substring(index1) : "");
    }


    /**
     * 从URI中获取模块和接口名
     *
     * @param method URI对应的函数
     * @return _1是模块名，_2是接口名
     */
    public static Tuple.Tuple2<String, String> getModuleInfo(HttpServletRequest request, Method method) {
        if (null == request) {
            return new Tuple.Tuple2<String, String>("", "");
        }
        // 分隔path,path中第一个内容为模块名，后面部分内容作为接口名
        String reqUri = request.getRequestURI();
        String mod_name = "unknown";
        String svc_name = "/";
        // 需要处理/a/b/c,a/b/c,a,/多种情况
        if (reqUri != null) {
            int bpos = 0;
            int pos = -1;
            // 如果以/开始，则忽略第一个/
            if (reqUri.startsWith("/")) {
                pos = reqUri.indexOf('/', 1);
                bpos = 1;
            } else {
                bpos = 0;
                pos = reqUri.indexOf('/');
            }

            if (pos >= 0) {
                mod_name = reqUri.substring(bpos, pos);
                svc_name = reqUri.substring(pos + 1);
            } else {
                svc_name = reqUri;
            }
        }

        if (StringUtils.isBlank(svc_name)) {
            svc_name = "/";
        }

        // 如果是rpc调用，则url中只有接口url,需要加上调用函数
        if (RequestUtil.isRpcCall(request) && method != null) {
            svc_name = svc_name + "#" + method.getName();
        }
        return new Tuple.Tuple2<String, String>(mod_name, svc_name);
    }
}
