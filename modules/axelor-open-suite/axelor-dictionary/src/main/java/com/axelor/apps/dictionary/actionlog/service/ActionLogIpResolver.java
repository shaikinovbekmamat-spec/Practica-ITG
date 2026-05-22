package com.axelor.apps.dictionary.actionlog.service;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;

@Singleton
public class ActionLogIpResolver {

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public ActionLogIpResolver(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public String resolve() {
    HttpServletRequest request = requestProvider.get();
    if (request == null) return "unknown";

    String ip = request.getHeader("X-Forwarded-For");

    if (ip != null && !ip.isBlank()) {
      return ip.split(",")[0].trim();
    }

    ip = request.getHeader("X-Real-IP");

    if (ip != null && !ip.isBlank()) {
      return ip;
    }

    // клиентский IP не найден → fallback на server IP
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      return request.getRemoteAddr();
    }
  }
}