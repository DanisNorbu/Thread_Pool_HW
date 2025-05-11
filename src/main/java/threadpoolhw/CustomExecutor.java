package main.java.threadpoolhw;

import java.util.List;
import java.util.concurrent.*;

/**
 * Интерфейс для пользовательского пула потоков.
 */
public interface CustomExecutor extends Executor {
    /**
     * Отправляет задачу Runnable на выполнение.
     */
    @Override
    void execute(Runnable command);

    /**
     * Отправляет задачу Callable на выполнение и возвращает Future.
     */
    <T> Future<T> submit(Callable<T> callable);

    /**
     * Инициирует упорядоченное завершение: новые задачи не принимаются, существующие задачи выполняются до конца.
     */
    void shutdown();

    /**
     * Пытается остановить все выполняющиеся задачи и прекращает обработку ожидающих задач.
     * @return список задач, которые не были выполнены.
     */
    List<Runnable> shutdownNow();
}