package com.axelor.apps.dictionary.actionlog.service;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ActionLogExecutorService {

  private static final Logger log = LoggerFactory.getLogger(ActionLogExecutorService.class);
  private final ExecutorService executor = Executors.newFixedThreadPool(3);

  public void submit(Runnable task) {
    executor.submit(task);
  }

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down ActionLogExecutorService...");
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
