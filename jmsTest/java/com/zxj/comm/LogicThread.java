package com.zxj.comm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang4838223 on 2016/7/7.
 */
public class LogicThread {


    private final static Log logger = LogFactory.getLog(LogicThread.class);

    /** 逻辑线程 */
    private static ThreadPoolExecutor logicExecutor = new ThreadPoolExecutor(1,
            1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    /** 执行延迟任务的辅助线程 */
   /* private static DelayedTaskThread timer = new DelayedTaskThread(
            logicExecutor);
*/
    private static boolean dynamicOverload;
    private static boolean staticOverload;

    /*public static void init(){
        timer.start();
    }*/
    /**
     * 处理上次未保存成功的备份数据
     */
    private static void handleBackupData() {

    }

    public static void scheduleTask(DelayedTask task) {
//        timer.putTask(task);
    }

    /**
     * 停止辅助线程，主逻辑线程工作
     */
    public static void shutdown() {
//        timer.setWork(false);
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
        System.out.println("logic queue size: "+ getLogicQueueSize());
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
