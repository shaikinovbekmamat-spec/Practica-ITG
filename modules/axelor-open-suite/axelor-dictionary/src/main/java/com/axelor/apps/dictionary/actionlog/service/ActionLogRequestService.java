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
