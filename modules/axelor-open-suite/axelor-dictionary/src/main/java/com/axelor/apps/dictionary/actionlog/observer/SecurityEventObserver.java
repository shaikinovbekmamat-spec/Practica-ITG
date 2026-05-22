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
package com.axelor.apps.dictionary.actionlog.observer;

import com.axelor.apps.dictionary.actionlog.dto.ActionLogData;
import com.axelor.apps.dictionary.actionlog.service.ActionLogContextService;
import com.axelor.apps.dictionary.actionlog.service.ActionLogService;
import com.axelor.event.Observes;
import com.axelor.events.LogoutEvent;
import com.axelor.events.PostLogin;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SecurityEventObserver {

  private static final Logger log = LoggerFactory.getLogger(SecurityEventObserver.class);

  @Inject private ActionLogService actionLogService;
  @Inject private ActionLogContextService contextService;

  public void onLoginSuccess(@Observes @Named(PostLogin.SUCCESS) PostLogin event) {
    final String username =
        event.getUser() != null && event.getUser().getCode() != null
            ? event.getUser().getCode()
            : event.getPrincipal() != null ? String.valueOf(event.getPrincipal()) : null;
    final Long userId = event.getUser() != null ? event.getUser().getId().longValue() : null;

    saveLog("LOGIN", "PostLogin.SUCCESS", userId, username);
  }

  public void onLogout(@Observes LogoutEvent event) {
    final String username =
        event.getUser() != null && event.getUser().getCode() != null
            ? event.getUser().getCode()
            : event.getPrincipal() != null ? String.valueOf(event.getPrincipal()) : null;
    final Long userId = event.getUser() != null ? event.getUser().getId().longValue() : null;

    saveLog("LOGOUT", "LogoutEvent", userId, username);
  }

  private void saveLog(String action, String serviceName, Long userId, String username) {
    try {
      ActionLogData data = new ActionLogData();
      data.setAction(action);
      data.setUserId(userId);
      data.setIpAddress(contextService.getClientIp());
      data.setHttpMethod(contextService.getHttpMethod());
      data.setServiceName(serviceName);
      data.setRequestBody(buildRequestBody(username));

      actionLogService.processLog(data);
    } catch (Exception e) {
      log.error("Failed to save security event log for action {}", action, e);
    }
  }

  private String buildRequestBody(String username) {
    if (username == null || username.isBlank()) {
      return "{}";
    }

    return "{\"username\":\"" + escapeJson(username) + "\"}";
  }

  private String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
