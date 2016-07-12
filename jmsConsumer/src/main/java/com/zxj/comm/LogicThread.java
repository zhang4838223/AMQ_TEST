package com.zxj.comm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/7/8.
 */
public class LogicThread {


    private final static Log logger = LogFactory.getLog(LogicThread.class);

    /** 逻辑线程 */
    private static ThreadPoolExecutor logicExecutor = new ThreadPoolExecutor(2,
            4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private static boolean dynamicOverload;
    private static boolean staticOverload;

    /**
     * 处理上次未保存成功的备份数据
     */
    private static void handleBackupData() {

    }

    public static void scheduleTask(DelayedTask task) {

    }

    /**
     * 停止辅助线程，主逻辑线程工作
     */
    public static void shutdown() {
        logicExecutor.shutdownNow();
    }

    /**
     * 使用主逻辑线程执行
     *
     * @param runable
     */
    public static void execute(Runnable runable) {
        if (runable == null || logicExecutor.isShutdown()) {
            return;
        }

        logicExecutor.execute(runable);
    }

    /**
     * 设置动态过载状态
     *
     * @param b
     */
    public static void setDynamicOverload(boolean b) {
        dynamicOverload = b;
    }

    /**
     * 设置静态过载状态
     *
     * @param b
     */
    public static void setStaticOverload(boolean b) {
        staticOverload = b;
    }

    public static int getLogicQueueSize() {
        return logicExecutor.getQueue().size();
    }

    public static boolean isTerminated() {
        return logicExecutor.isTerminated();
    }

}
