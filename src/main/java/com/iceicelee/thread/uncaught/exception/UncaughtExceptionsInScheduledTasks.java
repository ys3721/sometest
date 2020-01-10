package com.iceicelee.thread.uncaught.exception;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author: Yao Shuai
 * @date: 2020/1/9 16:13
 *
 * 用来测试当ScheduledExecutorService周期任务中抛异常之后的情况。
 * 结论就是无论如何runnable中抛出异样，该runnable以后的周期任务都会被取消。
 * 如果想要查看抛出什么异常可以使用其返回的feature.get(),然后try catch其中的异常信息，但这并不等于捕获了异常，该周期任务还是会被取消
 *
 */
public class UncaughtExceptionsInScheduledTasks {

    private ScheduledExecutorService defaultSchedule;

    private ScheduledExecutorService threadCaughtSchedule;

    private Runnable exceptionThrower;

    public UncaughtExceptionsInScheduledTasks() {
        defaultSchedule = Executors.newSingleThreadScheduledExecutor();
        threadCaughtSchedule = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r,"自己定义的thread，添加setUncaughtException来捕获异常。");
                thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println("thread.setUncaughtExceptionHandler抓到了异常！！");
                    }
                });
                return thread;
            }
        });

        exceptionThrower = () -> {
            for (int i = 0; i < 20; i ++) {
                System.out.println(i);
                if (i == 14) {
                    throw new NullPointerException("经典的空指针哇！");
                }
            }
        };
    }

    public void testDefaultSchedule() {
        defaultSchedule.scheduleAtFixedRate(exceptionThrower, 0, 1, TimeUnit.SECONDS);
    }

    public void testThreadCaughtSchedule() {
        threadCaughtSchedule .scheduleAtFixedRate(exceptionThrower, 0, 1, TimeUnit.SECONDS);
    }

    public void testReceiveTheException() {
        final ScheduledFuture<?> future = defaultSchedule.scheduleAtFixedRate(exceptionThrower,
                0, 1, TimeUnit.SECONDS);
        defaultSchedule.execute(() -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        UncaughtExceptionsInScheduledTasks ut = new UncaughtExceptionsInScheduledTasks();
        //这个默认的如果异常了啥也不提示，后续的执行也取消了
        ut.testDefaultSchedule();
        //使用setUncaughtExceptionHandler的呢？
        ut.testThreadCaughtSchedule();
        //获取到异常，而不是捕获到异常，这里周期该任务还是会停掉,只是能看到啥Exception而已
        ut.testReceiveTheException();
    }


}
