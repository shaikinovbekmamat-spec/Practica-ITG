/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
