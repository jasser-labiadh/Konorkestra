package dev.jasser.configDaemon;

import java.util.concurrent.*;
import dev.jasser.configDaemon.LogManager;
public class ClientWorkerPool {
    private final ExecutorService executor;
    private final LogManager logManager;
    public ClientWorkerPool(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.logManager = LogManager.getInstance();
    }
    public void submit(Runnable task) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logManager.logError("task failed : "+e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}