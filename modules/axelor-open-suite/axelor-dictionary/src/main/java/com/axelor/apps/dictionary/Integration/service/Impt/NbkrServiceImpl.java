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
package com.axelor.apps.dictionary.Integration.service.Impt;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.dictionary.Integration.Helper.XmlHelper;
import com.axelor.apps.dictionary.Integration.dto.NbkrRatesDto;
import com.axelor.apps.dictionary.Integration.http.NbkrHttpClient;
import com.axelor.apps.dictionary.Integration.persistence.NbkrPersistenceService;
import com.axelor.apps.dictionary.Integration.service.NbkrService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NbkrServiceImpl implements NbkrService {

  private static final Logger LOG = LoggerFactory.getLogger(NbkrServiceImpl.class);
  private static final String NBKR_BASE_URL = "https://www.nbkr.kg/XML/";

  private final NbkrHttpClient httpClient;
  private final NbkrPersistenceService persistenceService;

  @Inject
  public NbkrServiceImpl(NbkrHttpClient httpClient, NbkrPersistenceService persistenceService) {
    this.httpClient = httpClient;
    this.persistenceService = persistenceService;
  }

  @Override
  public void updateRates() throws AxelorException {
    fetchAndProcess("daily.xml");
    fetchAndProcess("weekly.xml");
  }

  private void fetchAndProcess(String type) throws AxelorException {
    String url = NBKR_BASE_URL + type;

    try {
      String xml = httpClient.fetchXml(url);

      // Высший уровень: Автоматический маппинг через JAXB
      NbkrRatesDto ratesDto = XmlHelper.unmarshal(xml, NbkrRatesDto.class);

      if (ratesDto != null) {
        persistenceService.saveRates(ratesDto, type);
        LOG.info("Successfully processed rates from {}", type);
      }
    } catch (Exception e) {
      LOG.error("Error processing NBKR rates from {}", url, e);
      throw new AxelorException(1, "Failed to process rates: " + e.getMessage());
    }
  }
}
