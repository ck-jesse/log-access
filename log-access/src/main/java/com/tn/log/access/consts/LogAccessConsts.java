package com.tn.log.access.consts;

/**
 * 相关的常量
 *
 * @author chenck
 * @date 2019/5/11 10:27
 */
public class LogAccessConsts {

    /**
     * 配置是否需要打印入参
     */
    public static final String PRINT_INPUT_ARGS_ENABLE = "log.access.print.input.args.enable";
    /**
     * 对于集合输出到日志中的最大元素大小
     */
    public static final String COLLECTION_MAX_SIZE = "log.access.print.collection.max.size";
    public static final int COLLECTION_MAX_SIZE_DEFAULT = 3;

    /**
     * 对于字符串输出到日志中的最大长度
     */
    public static final String STRING_MAX_SIZE = "log.access.print.string.max.size";
    public static final int STRING_MAX_SIZE_DEFAULT = 256;

    /**
     * 执行结果 N 对应normal，表示正常响应
     */
    public static final String EXEC_RESULT_NORMAL = "N";
    /**
     * 执行结果 E 对一个error，表示出现异常
     */
    public static final String EXEC_RESULT_ERROR = "E";
}
