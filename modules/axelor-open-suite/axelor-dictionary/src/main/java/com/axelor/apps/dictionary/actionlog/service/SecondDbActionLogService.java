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

import com.axelor.apps.dictionary.actionlog.dto.ActionLogData;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SecondDbActionLogService {

  private static final Logger log = LoggerFactory.getLogger(SecondDbActionLogService.class);
  private final SecondDbConnectionService connectionService;
  private final ActionLogExecutorService executorService;

  @Inject
  public SecondDbActionLogService(
      SecondDbConnectionService connectionService, ActionLogExecutorService executorService) {
    this.connectionService = connectionService;
    this.executorService = executorService;
  }

  public void saveLog(ActionLogData data) {
    if (connectionService.getDataSource() == null) {
      log.warn("Secondary database logging skipped: datasource not initialized.");
      return;
    }

    executorService.submit(
        () -> {
          String sql =
              "INSERT INTO action_log (action, user_id, ip_address, http_method, request_body, service_name, created_on) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?)";

          try (Connection conn = connectionService.getDataSource().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getAction());
            ps.setObject(2, data.getUserId());
            ps.setString(3, data.getIpAddress());
            ps.setString(4, data.getHttpMethod());
            ps.setString(5, data.getRequestBody());
            ps.setString(6, data.getServiceName());
            ps.setTimestamp(7, Timestamp.valueOf(data.getCreatedOn()));

            ps.executeUpdate();
            log.debug("Action log saved to secondary DB: {}", data.getAction());
          } catch (Exception e) {
            log.error("Error saving log to secondary database in background", e);
          }
        });
  }

  public void updateError(ActionLogData data) {
    if (connectionService.getDataSource() == null) return;

    executorService.submit(
        () -> {
          String sql =
              "UPDATE action_log SET error_message = ?, stack_trace = ? WHERE action = ? AND created_on = ?";
          try (Connection conn = connectionService.getDataSource().getConnection();
              PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getErrorMessage());
            ps.setString(2, data.getStackTrace());
            ps.setString(3, data.getAction());
            ps.setTimestamp(4, Timestamp.valueOf(data.getCreatedOn()));

            ps.executeUpdate();
            log.debug("Action log error updated in secondary DB: {}", data.getAction());
          } catch (Exception e) {
            log.error("Error updating log error in secondary database in background", e);
          }
        });
  }

  @PreDestroy
  public void shutdown() {
    connectionService.close();
  }
}
