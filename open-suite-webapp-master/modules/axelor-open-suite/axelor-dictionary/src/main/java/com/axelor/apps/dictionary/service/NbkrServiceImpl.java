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
package com.axelor.apps.dictionary.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.dictionary.db.DicCurrency;
import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.apps.dictionary.db.repo.DicCurrencyRepository;
import com.axelor.apps.dictionary.db.repo.DicRateCurrencyRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.google.inject.persist.Transactional;

@Singleton
public class NbkrServiceImpl implements NbkrService {

  private static final Logger LOG = LoggerFactory.getLogger(NbkrServiceImpl.class);

  private final DicCurrencyRepository currencyRepo;
  private final DicRateCurrencyRepository rateRepo;

  @Inject
  public NbkrServiceImpl(DicCurrencyRepository currencyRepo, DicRateCurrencyRepository rateRepo) {
    this.currencyRepo = currencyRepo;
    this.rateRepo = rateRepo;
  }

  @Override
  @Transactional
  public void updateRates() throws AxelorException {
    String dailyUrl = AppSettings.get().get("nbkr.daily.url", "https://www.nbkr.kg/XML/daily.xml");
    String weeklyUrl =
        AppSettings.get().get("nbkr.weekly.url", "https://www.nbkr.kg/XML/weekly.xml");

    fetchAndProcess(dailyUrl, "Daily");
    fetchAndProcess(weeklyUrl, "Weekly");
  }

  private void fetchAndProcess(String url, String type) throws AxelorException {
    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        LOG.error("Failed to fetch rates from {}: status {}", url, response.statusCode());
        return;
      }

      LOG.info("XML Response received from: {}", url);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(response.body().getBytes("UTF-8")));

      Element root = doc.getDocumentElement();
      String dateStr = root.getAttribute("Date");
      LocalDate rateDate = LocalDate.now();
      try {
        // NBKR uses dd.MM.yyyy format
        rateDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LOG.info("Rate date from XML: {}", rateDate);
      } catch (Exception e) {
        LOG.warn("Failed to parse date {} from XML, using today.", dateStr);
      }

      NodeList currencies = doc.getElementsByTagName("Currency");
      LOG.info("Found {} currencies in XML", currencies.getLength());

      for (int i = 0; i < currencies.getLength(); i++) {
        Element currencyEl = (Element) currencies.item(i);

        // Get currency code from attribute
        String code = currencyEl.getAttribute("ISOCode");
        if (code == null || code.isEmpty()) {
          LOG.warn("Currency without ISOCode, skipping");
          continue;
        }

        // Get nominal (required field)
        NodeList nominalNodes = currencyEl.getElementsByTagName("Nominal");
        if (nominalNodes.getLength() == 0) {
          LOG.warn("No Nominal found for currency: {}", code);
          continue;
        }
        String nominalStr = nominalNodes.item(0).getTextContent();
        int nominal = Integer.parseInt(nominalStr);

        // Get value/rate (required field)
        NodeList valueNodes = currencyEl.getElementsByTagName("Value");
        if (valueNodes.getLength() == 0) {
          LOG.warn("No Value found for currency: {}", code);
          continue;
        }
        String valueStr = valueNodes.item(0).getTextContent().replace(",", ".");
        BigDecimal rate = new BigDecimal(valueStr);

        // Find or create currency
        DicCurrency currency = currencyRepo.all().filter("self.code = ?1", code).fetchOne();
        if (currency == null) {
          currency = new DicCurrency();
          currency.setCode(code);
          currency.setName(code); // Use code as name since Name element doesn't exist
          currency = currencyRepo.save(currency);
          LOG.info("Created new currency: {} ({})", code, code);
        }

        // Find or create rate record
        DicRateCurrency existingRate =
            rateRepo
                .all()
                .filter(
                    "self.currency = ?1 AND self.rateDate = ?2 AND self.type = ?3",
                    currency,
                    rateDate,
                    type)
                .fetchOne();

        if (existingRate == null) {
          DicRateCurrency newRate = new DicRateCurrency();
          newRate.setCurrency(currency);
          newRate.setRate(rate);
          newRate.setNominal(nominal);
          newRate.setRateDate(rateDate);
          newRate.setType(type);
          rateRepo.save(newRate);
          LOG.debug("Saved new rate for {}: {} = {}", code, nominal, rate);
        } else {
          existingRate.setRate(rate);
          existingRate.setNominal(nominal);
          rateRepo.save(existingRate);
          LOG.debug("Updated rate for {}: {} = {}", code, nominal, rate);
        }
      }

      LOG.info("Successfully processed {} rates from {}", currencies.getLength(), type);

    } catch (Exception e) {
      LOG.error("Error processing NBKR rates from {}", url, e);
      throw new AxelorException(1, "Failed to process rates: " + e.getMessage());
    }
  }
}
