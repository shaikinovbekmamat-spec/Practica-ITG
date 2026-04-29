package com.axelor.apps.dictionary.rateReport.controller;

import com.axelor.apps.dictionary.rateReport.helper.ReportHelper;
import com.axelor.apps.dictionary.rateReport.service.GroupedExchangeRateReportService;
import com.axelor.apps.dictionary.report.IDictionaryReport;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GroupedExchangeRateReportController {

  private GroupedExchangeRateReportService reportService;

  @Inject
  public GroupedExchangeRateReportController(GroupedExchangeRateReportService reportService) {
    this.reportService = reportService;
  }

  public void printReport(ActionRequest request, ActionResponse response) {
    ReportHelper.executeReport(
        request, 
        response, 
        "Grouped Exchange Rates Report", 
        IDictionaryReport.EXCHANGE_RATES_GROUPED_REPORT, 
        ids -> reportService.getGroupedRatesData(ids));
  }
}
