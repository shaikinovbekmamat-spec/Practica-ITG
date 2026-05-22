
package com.axelor.apps.dictionary.actionlog.interceptor;

import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.actionlog.dto.ActionLogData;
import com.axelor.apps.dictionary.actionlog.service.ActionLogContextService;
import com.axelor.apps.dictionary.actionlog.service.ActionLogService;
import com.axelor.common.StringUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ActionLogInterceptor implements MethodInterceptor {

    private static final ThreadLocal<Set<String>> ACTIVE_INVOCATIONS =
            ThreadLocal.withInitial(HashSet::new);

    @Inject
    private ActionLogService actionLogService;
    @Inject
    private ActionLogContextService contextService;

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
