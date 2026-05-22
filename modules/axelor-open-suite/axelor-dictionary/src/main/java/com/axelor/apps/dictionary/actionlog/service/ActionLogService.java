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
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
public class ActionLogService {

  private static final Logger log = LoggerFactory.getLogger(ActionLogService.class);
  private final PrimaryDbActionLogService primaryDbService;
  private final SecondDbActionLogService secondDbService;
  private final ActionLogExceptionService exceptionService;

  @Inject
  public ActionLogService(
      PrimaryDbActionLogService primaryDbService,
      SecondDbActionLogService secondDbService,
      ActionLogExceptionService exceptionService) {
    this.primaryDbService = primaryDbService;
    this.secondDbService = secondDbService;
    this.exceptionService = exceptionService;
  }

  public void processLog(ActionLogData data) throws Exception {
    if (data.getCreatedOn() == null) {
      data.setCreatedOn(LocalDateTime.now());
    }


    primaryDbService.saveLog(data);

    try {
      secondDbService.saveLog(data);
    } catch (Exception e) {
      log.error("Error saving log to secondary database", e);
    }
  }

  public void logError(ActionLogData data, Throwable e) {
    data.setErrorMessage(e.getMessage());
    data.setStackTrace(exceptionService.getStackTraceAsString(e));

    primaryDbService.updateError(data);
    secondDbService.updateError(data);
  }
}
