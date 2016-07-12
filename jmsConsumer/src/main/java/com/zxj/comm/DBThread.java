package com.zxj.comm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhang4838223 on 2016/7/3.
 */
public class DBThread {

    public static final int DB_THREAD_NUM = 8;

    protected static ExecutorService executor = Executors
            .newFixedThreadPool(DB_THREAD_NUM);

    private static AtomicLong queueSize = new AtomicLong();

    public static void setExecutor(ExecutorService executor) {
        DBThread.executor = executor;
    }

    public static void execute(Runnable runable) {
        execute(runable, null);
    }

    public static void execute(final Runnable dbTask,
                               final Runnable logicCallback) {
        queueSize.incrementAndGet();
        final Throwable trace = new Throwable();
        executor.execute(new Runnable() {

            public void run() {
                try {
                    if (dbTask != null) {
                        System.out.println("the dbtask num is: "+ queueSize.get());
                        dbTask.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    queueSize.decrementAndGet();
                    if (logicCallback != null) {
//                        LogicThread.execute(logicCallback);
                    }
                }
            }
        });
    }

    /**
     * 停止所有数据库线程，在此之前会执行完所有待执行任务但不再接受新的任务
     *
     */
    public static void shutdown() {
        executor.shutdown();
    }

    /**
     * 是否所有数据库线程都执行完毕，只有在shutdown方法调用后才有可能返回true
     *
     * @return
     */
    public static boolean isTerminated() {
        return executor.isTerminated();
    }

    public static boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * 队列长度
     *
     * @return
     */
    public static long getQueueSize() {
        return queueSize.get();
    }

}
