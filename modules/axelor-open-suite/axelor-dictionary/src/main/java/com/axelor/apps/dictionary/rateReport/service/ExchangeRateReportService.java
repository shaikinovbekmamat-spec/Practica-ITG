package com.axelor.apps.dictionary.rateReport.service;

import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.axelor.apps.dictionary.rateReport.dto.ExchangeRateReportData;
import com.axelor.apps.dictionary.rateReport.helper.ReportHelper;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateReportService {

  private DicRateCurrencyRepository rateRepo;

  @Inject
  public ExchangeRateReportService(DicRateCurrencyRepository rateRepo) {
    this.rateRepo = rateRepo;
  }

  public List<ExchangeRateReportData> getRatesData(List<Integer> ids) {
    List<DicRateCurrency> rates = ReportHelper.fetchByIds(rateRepo, ids);

    if (rates == null || rates.isEmpty()) return null;
    List<ExchangeRateReportData> dtos = new ArrayList<>();

    rates.forEach(
        r ->
            dtos.add(
                ExchangeRateReportData.builder()
                    .rateValue(r.getRate())
                    .rateDate(String.valueOf(r.getRateDate()))
                    .currencyName(r.getCurrency() != null ? r.getCurrency().getName() : null)
                    .currencyCode(r.getCurrency() != null ? r.getCurrency().getCode() : null)
                    .build()));
    return dtos;
  }
}
