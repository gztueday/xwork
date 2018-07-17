package com.bigdata.xwork.action.core;

import java.util.concurrent.*;

public class WorkExecutorPool {


    public static ExecutorService executorService = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            CreateWorkActionRunTask thread = new CreateWorkActionRunTask(r, "action-running-task-");

            return thread;
        }
    }, new ThreadPoolExecutor.AbortPolicy());

    public static class CreateWorkActionRunTask extends Thread {

        private static int runningTaskCount = 0;

        public CreateWorkActionRunTask(Runnable target, String name) {
            super(target, name + (runningTaskCount));
            runningTaskCount++;
        }
    }

    public static void submitWork(WorkActionBase action, int delay, TimeUnit unit) {

        executorService.submit(action);

    }

    public static void submitWork(Runnable runner) {
        executorService.submit(runner);

    }

}
