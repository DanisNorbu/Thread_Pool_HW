package main.java.threadpoolhw;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CustomExecutor pool = new CustomThreadPool(
                2, 4, 5, TimeUnit.SECONDS,
                5, 2, "MyPool"
        );

        // Отправка демо задач
        for (int i = 1; i <= 10; i++) {
            final int id = i;
            pool.execute(() -> {
                System.out.println("[Task] Старт задачи " + id);
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                System.out.println("[Task] Завершение задачи " + id);
            });
        }

        // Демонстрация отказа при перегрузке
        for (int i = 11; i <= 20; i++) {
            final int id = i;
            pool.execute(() -> System.out.println("[ExtraTask] Дополнительная задача " + id));
        }

        // Завершение работы пула
        Thread.sleep(10000);
        pool.shutdown();
        System.out.println("[Main] Вызван shutdown.");
    }
}
