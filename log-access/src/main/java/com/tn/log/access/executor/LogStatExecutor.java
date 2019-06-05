package com.tn.log.access.executor;

/**
 * 日志统计执行器
 *
 * @author chenck
 * @date 2019/5/13 10:19
 */
public interface LogStatExecutor {

    /**
     * 启动访问日志统计
     * <p/>
     * 注：日志处理过程中的异常会内部消化掉，以免影响业务方法
     *
     * @author chenck
     * @date 2019/5/13 10:23
     */
    public void startLogAccessStat();

    /**
     * 完成访问日志统计（打印）
     * <p/>
     * 注：日志处理过程中的异常会内部消化掉，以免影响业务方法
     *
     * @author chenck
     * @date 2019/5/13 10:23
     */
    public void finishLogAccessStat(Object result, Exception throwable);

}
