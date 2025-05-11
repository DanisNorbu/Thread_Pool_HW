package main.java.threadpoolhw;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

//Обработчик отказов, который запускает отклонённую задачу в потоке вызова.

public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println("[Rejected] Задача " + r + " была отклонена из-за перегрузки! Выполняется в потоке вызова.");
        r.run();
    }
}