package com.aidsync.util;

import javafx.concurrent.Task;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Performance utility for background operations and UI optimization
 */
public class PerformanceUtil {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceUtil.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "AIDSYNC-Background");
        t.setDaemon(true);
        return t;
    });
    
    /**
     * Execute database operations in background thread
     */
    public static <T> CompletableFuture<T> executeAsync(Supplier<T> operation) {
        return CompletableFuture.supplyAsync(operation, executor)
            .exceptionally(throwable -> {
                logger.error("Background operation failed", throwable);
                return null;
            });
    }
    
    /**
     * Execute UI updates on JavaFX Application Thread
     */
    public static void runOnUIThread(Runnable uiUpdate) {
        if (Platform.isFxApplicationThread()) {
            uiUpdate.run();
        } else {
            Platform.runLater(uiUpdate);
        }
    }
    
    /**
     * Create background task for long-running operations
     */
    public static <T> Task<T> createTask(Supplier<T> operation) {
        return new Task<T>() {
            @Override
            protected T call() throws Exception {
                return operation.get();
            }
        };
    }
    
    /**
     * Shutdown executor on application exit
     */
    public static void shutdown() {
        executor.shutdown();
    }
}