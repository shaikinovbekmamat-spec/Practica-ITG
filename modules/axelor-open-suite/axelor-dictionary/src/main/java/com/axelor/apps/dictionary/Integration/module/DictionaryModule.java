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
package com.axelor.apps.dictionary.Integration.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.dictionary.Integration.service.Impt.NbkrServiceImpl;
import com.axelor.apps.dictionary.Integration.service.NbkrService;
import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.actionlog.interceptor.ActionLogInterceptor;
import com.axelor.apps.dictionary.actionlog.observer.SecurityEventObserver;
import com.axelor.apps.dictionary.actionlog.service.ActionLogContextService;
import com.axelor.apps.dictionary.actionlog.service.ActionLogService;
import com.axelor.apps.dictionary.actionlog.service.SecondDbActionLogService;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepositoryRepo;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.axelor.apps.dictionary.rateReport.controller.ExchangeRateReportController;
import com.axelor.apps.dictionary.rateReport.controller.GroupedExchangeRateReportController;
import com.axelor.apps.dictionary.rateReport.service.ExchangeRateReportService;
import com.axelor.apps.dictionary.rateReport.service.GroupedExchangeRateReportService;
import com.google.inject.matcher.Matchers;

public class DictionaryModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(NbkrService.class).to(NbkrServiceImpl.class);
    bind(DicRateCurrencyRepository.class).to(DicRateCurrencyRepositoryRepo.class);

    bind(ExchangeRateReportService.class);
    bind(GroupedExchangeRateReportService.class);

    bind(ExchangeRateReportController.class);
    bind(GroupedExchangeRateReportController.class);

    bind(ActionLogContextService.class);
    bind(SecondDbActionLogService.class);
    bind(ActionLogService.class);
    bind(SecurityEventObserver.class).asEagerSingleton();

    ActionLogInterceptor interceptor = new ActionLogInterceptor();
    requestInjection(interceptor);
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(ActionLog.class), interceptor);
  }
}
