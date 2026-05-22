package com.axelor.apps.dictionary.actionlog.service;

import com.axelor.app.AppSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SecondDbConnectionService {

  private static final Logger log = LoggerFactory.getLogger(SecondDbConnectionService.class);
  private HikariDataSource dataSource;

  public SecondDbConnectionService() {
    initDataSource();
  }

  private void initDataSource() {
    try {
      HikariConfig hikariConfig = new HikariConfig();
      AppSettings settings = AppSettings.get();

      hikariConfig.setJdbcUrl(
          settings.get("second.db.url", "jdbc:postgresql://localhost:5432/external_logs"));
      hikariConfig.setUsername(settings.get("second.db.user", "postgres"));
      hikariConfig.setPassword(settings.get("second.db.password", "5555"));

      hikariConfig.setDriverClassName("org.postgresql.Driver");
      hikariConfig.setMaximumPoolSize(5);
      hikariConfig.setConnectionTimeout(2000);

      this.dataSource = new HikariDataSource(hikariConfig);
      log.info("Secondary database datasource initialized successfully.");
    } catch (Exception e) {
      log.error("Failed to initialize secondary database datasource", e);
    }
  }

  public HikariDataSource getDataSource() {
    return dataSource;
  }

  public void close() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}
