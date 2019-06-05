package com.tn.log.access.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 字符串工具类
 *
 * @author chenck
 * @date 2019/5/10 11:53
 */
public class StrUtils {
    public static final String EMPTY = "";

    /**
     * null转换为0长字符串
     *
     * @param value
     * @return
     */
    public static String null2Empty(String value) {
        if (value == null) {
            return EMPTY;
        }

        return value;
    }

    /**
     * urldecode处理
     *
     * @param value
     * @param charset
     * @return
     */
    public static String urlDecode(String value, String charset) {
        try {
            return URLDecoder.decode(value, charset);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * 替换字串片段
     *
     * @param src         替换的源串
     * @param replacement 替换的目标字符
     * @param start       替换的片段开始位置,包含该位置字符
     * @param end         替换的片段地束位置,不含该位置字符
     * @return 替换后的串
     */
    public static String replacePiece(final String src, final char replacement, int start, int end) {
        if (src == null) {
            return src;
        }

        final StringBuilder sb = new StringBuilder(src.length());
        for (int i = 0; i < src.length(); ++i) {
            //不在指定范围的则直接原样复制
            if (i < start || i >= end) {
                sb.append(src.charAt(i));
                continue;
            }

            sb.append(replacement);
        }

        return sb.toString();
    }

}
