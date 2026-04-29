package com.axelor.apps.dictionary.rateReport.service;

import com.axelor.apps.dictionary.db.DicCurrency;
import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.apps.dictionary.db.repo.DicCurrencyRepository;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.axelor.apps.dictionary.rateReport.dto.ExchangeRateReportData;
import com.axelor.apps.dictionary.rateReport.helper.ReportHelper;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GroupedExchangeRateReportService {

  private DicCurrencyRepository currencyRepo;
  private DicRateCurrencyRepository rateRepo;

  @Inject
  public GroupedExchangeRateReportService(
      DicCurrencyRepository currencyRepo, DicRateCurrencyRepository rateRepo) {
    this.currencyRepo = currencyRepo;
    this.rateRepo = rateRepo;
  }

  public List<ExchangeRateReportData> getGroupedRatesData(List<Integer> ids) {
    List<DicCurrency> currencies = ReportHelper.fetchByIds(currencyRepo, ids);
    List<ExchangeRateReportData> dataList = new ArrayList<>();

    if (currencies == null) return dataList;

    for (DicCurrency currency : currencies) {
      List<DicRateCurrency> rates =
          rateRepo.all().filter("self.currency = :currency").bind("currency", currency).fetch();
      
      if (rates != null) {
        rates.forEach(r -> 
          dataList.add(ExchangeRateReportData.builder()
              .currencyCode(currency.getCode())
              .currencyName(currency.getName())
              .rateDate(String.valueOf(r.getRateDate()))
              .rateValue(r.getRate())
              .build())
        );
      }
    }
    return dataList;
  }
}
