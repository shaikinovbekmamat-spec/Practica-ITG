
package com.axelor.apps.dictionary.actionlog.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;

@Singleton
public class ActionLogRequestService {

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public ActionLogRequestService(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public String getHttpMethod() {
    HttpServletRequest request = requestProvider.get();
    return request != null ? request.getMethod() : "unknown";
  }

  public Long getCurrentUserId() {
    User user = AuthUtils.getUser();
    return user != null ? user.getId() : null;
  }
}
