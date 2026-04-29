package com.axelor.apps.dictionary.rateReport.controller;

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

  public void printReport(ActionRequest request, ActionResponse response) {
    ReportHelper.executeReport(
        request, 
        response, 
        "Exchange Rates Report", 
        IDictionaryReport.EXCHANGE_RATES_REPORT, 
        ids -> reportService.getRatesData(ids));
  }
}
