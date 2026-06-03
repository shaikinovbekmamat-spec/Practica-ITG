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
