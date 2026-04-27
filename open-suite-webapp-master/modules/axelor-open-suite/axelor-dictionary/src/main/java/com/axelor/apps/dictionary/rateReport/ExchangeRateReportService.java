package com.axelor.apps.dictionary.rateReport;

import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.axelor.common.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateReportService {

  private DicRateCurrencyRepository rateRepo;

  @Inject
  public ExchangeRateReportService(DicRateCurrencyRepository rateRepo) {
    this.rateRepo = rateRepo;
  }

  public String getRatesAsJson(List<Integer> ids) throws JsonProcessingException {
    List<DicRateCurrency> rates;
    
    if (ObjectUtils.isEmpty(ids)) {
       rates = rateRepo.all().fetch();
    } else {
       rates = ids.stream()
           .map(id -> rateRepo.find(id.longValue()))
           .collect(Collectors.toList());
    }

    List<Map<String, Object>> dataList = new ArrayList<>();
    for (DicRateCurrency rate : rates) {
      Map<String, Object> item = new HashMap<>();
      item.put("code", rate.getCurrency().getCode());
      item.put("rateDate", rate.getRateDate() != null ? rate.getRateDate().toString() : "");
      item.put("rate", rate.getRate());
      dataList.add(item);
    }

    return new ObjectMapper().writeValueAsString(dataList);
  }
}
