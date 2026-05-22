package com.axelor.apps.dictionary.actionlog.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActionLogContextService {

  private final ActionLogIpResolver ipResolver;
  private final ActionLogRequestService requestService;
  private final ActionLogSerializerService serializerService;

  @Inject
  public ActionLogContextService(
      ActionLogIpResolver ipResolver,
      ActionLogRequestService requestService,
      ActionLogSerializerService serializerService) {
    this.ipResolver = ipResolver;
    this.requestService = requestService;
    this.serializerService = serializerService;
  }

  public String getClientIp() {
    return ipResolver.resolve();
  }

  public Long getCurrentUserId() {
    return requestService.getCurrentUserId();
  }

  public String getHttpMethod() {
    return requestService.getHttpMethod();
  }

  public String extractRequestBody(Object[] arguments) {
    return serializerService.extractRequestBody(arguments);
  }
}
