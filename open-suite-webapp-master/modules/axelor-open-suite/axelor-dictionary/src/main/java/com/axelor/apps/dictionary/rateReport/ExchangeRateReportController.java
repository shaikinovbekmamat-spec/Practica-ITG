package com.axelor.apps.dictionary.rateReport;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.dictionary.report.IDictionaryReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ExchangeRateReportController {

  private ExchangeRateReportService reportService;

  @Inject
  public ExchangeRateReportController(ExchangeRateReportService reportService) {
    this.reportService = reportService;
  }

  public void printReport(ActionRequest request, ActionResponse response) {
    try {
      // 1. Получаем JSON данные через сервис
      @SuppressWarnings("unchecked")
      List<Integer> ids = (List<Integer>) request.getContext().get("_ids");
      String jsonData = reportService.getRatesAsJson(ids);

      String title = I18n.get("Exchange Rates Report");

      // 2. Генерация отчета напрямую через фабрику (по паттерну AccountingReportTypeController)
      String fileLink = 
          ReportFactory.createReport(IDictionaryReport.EXCHANGE_RATES_REPORT, title + "-${date}")
              .addFormat(ReportSettings.FORMAT_PDF)
              .addParam("DATA_SOURCE", jsonData)
              .addParam("__locale", ReportSettings.getPrintingLocale())
              .generate()
              .getFileLink();

      response.setView(ActionView.define(title).add("html", fileLink).map());

    } catch (Exception e) {
      response.setError(e.getMessage());
    }
  }
}
