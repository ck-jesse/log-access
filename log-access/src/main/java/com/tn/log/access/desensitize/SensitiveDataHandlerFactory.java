package com.tn.log.access.desensitize;

import com.tn.log.access.desensitize.handler.IdNoHandler;
import com.tn.log.access.desensitize.handler.MobileHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供一个敏感数据格式化处理的工厂，将常规的直接初始化到工厂中
 *
 * @author chenck
 * @date 2019/5/10 11:57
 */
public class SensitiveDataHandlerFactory {
    private final static Map<Integer, Handler> cacheHandler = new HashMap<Integer, Handler>();

    static {
        cacheHandler.put(SensitiveDataType.DT_MOBILE, new MobileHandler());
        cacheHandler.put(SensitiveDataType.DT_IDNO, new IdNoHandler());
    }

    public static Handler getHandler(int type) {
        if (type <= 0) {
            return null;
        }

        return cacheHandler.get(type);
    }

    public static void addHandler(int type, Handler formatter) {
        cacheHandler.put(type, formatter);
    }
}
