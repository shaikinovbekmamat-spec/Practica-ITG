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
package com.axelor.apps.dictionary.rateReport.controller;

import com.axelor.apps.dictionary.db.DicCurrency;
import com.axelor.apps.dictionary.db.repo.DicCurrencyRepository;
import com.axelor.apps.dictionary.rateReport.service.ImageReportService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ImageReportController {

  private DicCurrencyRepository currencyRepo;
  private ImageReportService imageReportService;

  @Inject
  public ImageReportController(
      DicCurrencyRepository currencyRepo, ImageReportService imageReportService) {
    this.currencyRepo = currencyRepo;
    this.imageReportService = imageReportService;
  }

  public void showImage(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Object formatObj = context.get("$reportFormat");
    String format = formatObj != null ? formatObj.toString() : ReportSettings.FORMAT_PDF;
    DicCurrency currency = context.asType(DicCurrency.class);

    if (currency == null || currency.getId() == null) {
      response.setError("Please save the record first.");
      return;
    }

    generateImageReport(response, currency.getId(), format, false);
  }

  public void downloadImagePdf(ActionRequest request, ActionResponse response) {
    downloadImageReport(request, response, ReportSettings.FORMAT_PDF, false);
  }

  public void downloadImageWord(ActionRequest request, ActionResponse response) {
    downloadImageReport(request, response, ReportSettings.FORMAT_DOCX, true);
  }

  private void downloadImageReport(
      ActionRequest request, ActionResponse response, String format, boolean download) {
    Long currencyId = getCurrencyId(request.getContext());
    if (currencyId == null) {
      response.setError("Please save the record first.");
      return;
    }

    generateImageReport(response, currencyId, format, download);
  }

  private Long getCurrencyId(Context context) {
    Object currencyIdObj = context.get("_currencyId");
    Object currencyIdsObj = context.get("_currencyIds");

    if (currencyIdObj == null
        && currencyIdsObj instanceof List<?> currencyIds
        && !currencyIds.isEmpty()) {
      currencyIdObj = currencyIds.get(0);
    }

    if (currencyIdObj == null) {
      return null;
    }

    return Long.valueOf(currencyIdObj.toString());
  }

  private void generateImageReport(
      ActionResponse response, Long currencyId, String format, boolean download) {
    DicCurrency currency = currencyRepo.find(currencyId);
    if (currency == null) {
      response.setError("Please save the record first.");
      return;
    }

    MetaFile image = currency.getImage();

    if (image == null) {
      response.setError("No image uploaded for this currency.");
      return;
    }

    try {
      String title = "Image: " + currency.getName();
      String fileLink = imageReportService.generateImageReport(currency, format);

      ActionViewBuilder viewBuilder = ActionView.define(title).add("html", fileLink);
      if (download || !ReportSettings.FORMAT_PDF.equalsIgnoreCase(format)) {
        viewBuilder.param("download", "true");
      }

      response.setView(viewBuilder.map());
    } catch (Exception e) {
      e.printStackTrace();
      response.setError("Error generating report: " + e.getMessage());
    }
  }
}
