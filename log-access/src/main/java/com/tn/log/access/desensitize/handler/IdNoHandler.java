package com.tn.log.access.desensitize.handler;

import com.tn.log.access.desensitize.Handler;
import com.tn.log.access.util.StrUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 身份证号 15位或18位
 * 香港身份证
 * P103265(1)
 * 回乡证号
 * H0946839900
 * 护照号码的格式：
 * 因私普通护照号码格式有:14/15+7位数,G+8位数；因公普通的是:P.+7位数；
 * 公务的是：S.+7位数 或者 S+8位数,以D开头的是外交护照.D=diplomatic
 *
 * @author chenck
 * @date 2019/5/10 11:55
 */
public class IdNoHandler implements Handler<String, String> {
    @Override
    public String handler(String value, String fmt) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        //证件号展示前4位和后2位，当与码小于等于18位时只展示前2位和后1位，小于3位时将展示全部
        int start = value.length() <= 8 ? 2 : 4;
        int end = value.length() > 15 ? value.length() - 2 : value.length() - 1;
        return StrUtils.replacePiece(value, '*', start, end);
    }
}
