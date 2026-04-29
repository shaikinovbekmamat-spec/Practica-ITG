package com.axelor.apps.dictionary.rateReport.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateReportData {

  private String currencyCode;
  private String currencyName;
  private String rateDate;
  private BigDecimal rateValue;
}
