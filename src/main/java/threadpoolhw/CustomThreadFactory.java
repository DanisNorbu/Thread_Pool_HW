package main.java.threadpoolhw;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Фабрика для создания потоков с уникальными именами и логированием их создания и завершения.
 */
public class CustomThreadFactory implements ThreadFactory {
    private final String poolName;
    private final AtomicInteger counter = new AtomicInteger(0);

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = poolName + "-worker-" + counter.incrementAndGet();
        Thread t = new Thread(r, name);
        System.out.println("[ThreadFactory] Создаётся новый поток: " + name);
        t.setUncaughtExceptionHandler((thread, ex) ->
                System.err.println("[ThreadFactory] Поток " + thread.getName() + " завершился с ошибкой: " + ex)
        );
        return t;
    }
}