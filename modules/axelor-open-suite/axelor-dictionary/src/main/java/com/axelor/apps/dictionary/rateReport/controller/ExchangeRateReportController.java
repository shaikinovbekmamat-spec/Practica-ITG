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
package com.axelor.apps.dictionary.rateReport.controller;

import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.rateReport.helper.ReportHelper;
import com.axelor.apps.dictionary.rateReport.service.ExchangeRateReportService;
import com.axelor.apps.dictionary.report.IDictionaryReport;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ExchangeRateReportController {

  private ExchangeRateReportService reportService;

  @Inject
  public ExchangeRateReportController(ExchangeRateReportService reportService) {
    this.reportService = reportService;
  }

  @ActionLog
  public void printReport(ActionRequest request, ActionResponse response) {
    ReportHelper.executeReport(
        request,
        response,
        "Exchange Rates Report",
        IDictionaryReport.EXCHANGE_RATES_REPORT,
        ids -> reportService.getRatesData(ids));
  }
}
