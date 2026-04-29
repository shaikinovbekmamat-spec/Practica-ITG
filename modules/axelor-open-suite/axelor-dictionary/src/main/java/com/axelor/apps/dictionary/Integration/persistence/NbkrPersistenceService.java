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
package com.axelor.apps.dictionary.Integration.persistence;

import com.axelor.apps.dictionary.Integration.dto.NbkrCurrencyDto;
import com.axelor.apps.dictionary.Integration.dto.NbkrRatesDto;
import com.axelor.apps.dictionary.db.DicCurrency;
import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.apps.dictionary.db.repo.DicCurrencyRepository;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NbkrPersistenceService {
  private static final Logger LOG = LoggerFactory.getLogger(NbkrPersistenceService.class);

  private final DicCurrencyRepository currencyRepo;
  private final DicRateCurrencyRepository rateRepo;

  @Inject
  public NbkrPersistenceService(
      DicCurrencyRepository currencyRepo, DicRateCurrencyRepository rateRepo) {
    this.currencyRepo = currencyRepo;
    this.rateRepo = rateRepo;
  }

  @Transactional
  public void saveRates(NbkrRatesDto ratesDto, String type) {
    for (NbkrCurrencyDto currencyDto : ratesDto.getCurrencies()) {
      DicCurrency currency =
          currencyRepo.all().filter("self.code = ?1", currencyDto.getCode()).fetchOne();
      if (currency == null) {
        currency = new DicCurrency();
        currency.setCode(currencyDto.getCode());
        currency.setName(currencyDto.getCode());
        currency = currencyRepo.save(currency);
        LOG.info("Created new currency: {}", currencyDto.getCode());
      }

      DicRateCurrency existingRate =
          rateRepo
              .all()
              .filter(
                  "self.currency = ?1 AND self.rateDate = ?2 AND self.type = ?3",
                  currency,
                  ratesDto.getDate(),
                  type)
              .fetchOne();

      if (existingRate == null) {
        DicRateCurrency newRate = new DicRateCurrency();
        newRate.setCurrency(currency);
        newRate.setRate(currencyDto.getRate());
        newRate.setNominal(currencyDto.getNominal());
        newRate.setRateDate(ratesDto.getDate());
        newRate.setType(type);
        rateRepo.save(newRate);
        LOG.debug(
            "Saved new rate for {}: {} = {}",
            currencyDto.getCode(),
            currencyDto.getNominal(),
            currencyDto.getRate());
      } else {
        existingRate.setRate(currencyDto.getRate());
        existingRate.setNominal(currencyDto.getNominal());
        rateRepo.save(existingRate);
        LOG.debug(
            "Updated rate for {}: {} = {}",
            currencyDto.getCode(),
            currencyDto.getNominal(),
            currencyDto.getRate());
      }
    }
  }
}
