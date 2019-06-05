package com.tn.log.access.desensitize.handler;

import com.tn.log.access.desensitize.Handler;
import com.tn.log.access.util.StrUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 手机号脱敏处理器
 * 1、手机前3位号码代表运营商，如130代表联通，133代表电信，134代表移动
 * 2、中间4位号码（第4到第7），代表手机号码归属地的区号，如号码1399294****这个号码，中间4位是9294，经查询是陕西铜川地区的移动编号。
 * 3、最后4位数字代表移动电话用户，由归属位置寄存器HLR（Home Location Register）进行自由分配。
 *
 * @author chenck
 * @date 2019/5/10 11:53
 */
public class MobileHandler implements Handler<String, String> {
    @Override
    public String handler(String value, String fmt) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        int len = value.length();
        int start = 0;
        int end = 0;
        //超过8位，隐藏倒数第8位以第4位
        if (len >= 8) {
            start = len - 8;
            end = len - 4;
        } else if (len > 4) {
            //小于8位大于4位，直接隐藏最后4位之前的所有
            start = 0;
            end = len - 4;
        } else {
            //小于4位则不隐藏
            return value;
        }

        return StrUtils.replacePiece(value, '*', start, end);
    }
}
