package main.java.threadpoolhw;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация пользовательского пула потоков с очередями на каждый поток, масштабированием и логированием.
 */
public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;

    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectionHandler;

    private final List<Worker> workers = new ArrayList<>();
    private final AtomicInteger nextWorkerIndex = new AtomicInteger(0);
    private final AtomicInteger busyCount = new AtomicInteger(0);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final Object lock = new Object();

    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                            int queueSize, int minSpareThreads, String poolName) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = new CustomThreadFactory(poolName);
        this.rejectionHandler = new CustomRejectedExecutionHandler();

        // Предварительный запуск резервных потоков
        for (int i = 0; i < minSpareThreads; i++) {
            addWorker();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown.get()) {
            rejectionHandler.rejectedExecution(command, null);
            return;
        }
        synchronized (lock) {
            int freeThreads = workers.size() - busyCount.get();
            if (freeThreads < minSpareThreads && workers.size() < maxPoolSize) {
                addWorker();
            }
        }
        Worker worker = chooseWorker();
        if (!worker.offer(command)) {
            synchronized (lock) {
                if (workers.size() < maxPoolSize) {
                    Worker newWorker = addWorker();
                    if (!newWorker.offer(command)) {
                        rejectionHandler.rejectedExecution(command, null);
                    }
                } else {
                    rejectionHandler.rejectedExecution(command, null);
                }
            }
        } else {
            System.out.println("[Pool] Задача принята в очередь " + worker.getName() + ": " + command);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<>(callable);
        execute(task);
        return task;
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
        synchronized (lock) {
            for (Worker w : workers) {
                w.signalShutdown();
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown.set(true);
        List<Runnable> pending = new ArrayList<>();
        synchronized (lock) {
            for (Worker w : workers) {
                pending.addAll(w.clearQueue());
                w.interrupt();
            }
            workers.clear();
        }
        return pending;
    }

    private Worker chooseWorker() {
        synchronized (lock) {
            int idx = nextWorkerIndex.getAndIncrement() % workers.size();
            return workers.get(idx);
        }
    }

    private Worker addWorker() {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);
        Worker w = new Worker(queue);
        workers.add(w);
        threadFactory.newThread(w).start();
        return w;
    }

    private class Worker extends Thread {
        private final BlockingQueue<Runnable> queue;
        private volatile boolean running = true;

        Worker(BlockingQueue<Runnable> queue) {
            this.queue = queue;
        }

        boolean offer(Runnable task) {
            return queue.offer(task);
        }

        void signalShutdown() {
            running = false;
            this.interrupt();
        }

        List<Runnable> clearQueue() {
            List<Runnable> list = new ArrayList<>();
            queue.drainTo(list);
            return list;
        }

        @Override
        public void run() {
            try {
                while (running && (!isShutdown.get() || !queue.isEmpty())) {
                    Runnable task = queue.poll(keepAliveTime, timeUnit);
                    if (task != null) {
                        busyCount.incrementAndGet();
                        System.out.println("[Worker] " + getName() + " выполняет " + task);
                        try {
                            task.run();
                        } finally {
                            busyCount.decrementAndGet();
                        }
                    } else {
                        synchronized (lock) {
                            if (workers.size() > corePoolSize) {
                                workers.remove(this);
                                System.out.println("[Worker] " + getName() + " превысил время простоя, завершение.");
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException ex) {
                // Выход при прерывании
            } finally {
                System.out.println("[Worker] " + getName() + " завершён.");
            }
        }
    }
}