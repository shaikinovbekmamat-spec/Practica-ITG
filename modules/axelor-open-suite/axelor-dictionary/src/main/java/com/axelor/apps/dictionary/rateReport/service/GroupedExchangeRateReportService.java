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
        rates.forEach(
            r ->
                dataList.add(
                    ExchangeRateReportData.builder()
                        .currencyCode(currency.getCode())
                        .currencyName(currency.getName())
                        .rateDate(String.valueOf(r.getRateDate()))
                        .rateValue(r.getRate())
                        .build()));
      }
    }
    return dataList;
  }
}
