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
package com.axelor.apps.dictionary.actionlog.interceptor;

import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.actionlog.dto.ActionLogData;
import com.axelor.apps.dictionary.actionlog.service.ActionLogContextService;
import com.axelor.apps.dictionary.actionlog.service.ActionLogService;
import com.axelor.common.StringUtils;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class ActionLogInterceptor implements MethodInterceptor {

  private static final ThreadLocal<Set<String>> ACTIVE_INVOCATIONS =
      ThreadLocal.withInitial(HashSet::new);

  @Inject private ActionLogService actionLogService;
  @Inject private ActionLogContextService contextService;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    ActionLog annotation = invocation.getMethod().getAnnotation(ActionLog.class);
    if (annotation == null) return invocation.proceed();

    String invocationKey = buildInvocationKey(invocation, annotation);
    Set<String> activeInvocations = ACTIVE_INVOCATIONS.get();
    if (!activeInvocations.add(invocationKey)) return invocation.proceed();

    ActionLogData logData = prepareLogData(invocation, annotation);

    try {
      actionLogService.processLog(logData);
    } catch (Exception e) {
      log.error("ActionLog FAIL: Could not save logs. Continuing method execution.", e);
    }

    try {
      return invocation.proceed();
    } catch (Throwable t) {
      try {
        actionLogService.logError(logData, t);
      } catch (Exception e) {
        log.error(
            "ActionLog FAIL: Could not save error details for action {}", logData.getAction(), e);
      }
      throw t;
    } finally {
      activeInvocations.remove(invocationKey);
      if (activeInvocations.isEmpty()) {
        ACTIVE_INVOCATIONS.remove();
      }
    }
  }

  private ActionLogData prepareLogData(MethodInvocation invocation, ActionLog annotation) {
    ActionLogData data = new ActionLogData();
    String action = annotation.action();
    if (action == null || action.isBlank()) {
      action =
          invocation.getMethod().getDeclaringClass().getSimpleName()
              + "."
              + invocation.getMethod().getName();
    }
    data.setAction(action);
    data.setUserId(contextService.getCurrentUserId());
    data.setIpAddress(contextService.getClientIp());
    data.setHttpMethod(contextService.getHttpMethod());
    data.setRequestBody(contextService.extractRequestBody(invocation.getArguments()));
    data.setServiceName(
        invocation.getMethod().getDeclaringClass().getSimpleName()
            + "."
            + invocation.getMethod().getName());
    return data;
  }

  private String buildInvocationKey(MethodInvocation invocation, ActionLog annotation) {
    String action = annotation.action();
    if (StringUtils.isBlank(action)) {
      action =
          invocation.getMethod().getDeclaringClass().getSimpleName()
              + "."
              + invocation.getMethod().getName();
    }

    StringBuilder keyBuilder = new StringBuilder(action);
    Object[] arguments = invocation.getArguments();
    if (arguments != null) {
      for (Object argument : arguments) {
        keyBuilder
            .append('|')
            .append(argument == null ? "null" : System.identityHashCode(argument));
      }
    }

    return keyBuilder.toString();
  }
}
