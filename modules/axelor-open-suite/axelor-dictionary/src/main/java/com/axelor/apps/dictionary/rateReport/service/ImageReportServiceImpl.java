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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.dictionary.db.DicCurrency;
import com.axelor.apps.dictionary.report.IDictionaryReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.FileInputStream;
import java.io.InputStream;

public class ImageReportServiceImpl implements ImageReportService {

  @Override
  public String generateImageReport(DicCurrency currency, String format) throws Exception {
    String title = "Image: " + currency.getName();
    ReportSettings reportSettings =
        ReportFactory.createReport(IDictionaryReport.IMAGE_VIEWER_REPORT, title + "-${date}")
            .addParam("IMAGE_ID", currency.getImage().getId().intValue())
            .addFormat(format)
            .generate();

    return getFileLink(reportSettings, format);
  }

  protected String getFileLink(ReportSettings reportSettings, String format) throws Exception {
    if (!ReportSettings.FORMAT_DOCX.equalsIgnoreCase(format)) {
      return reportSettings.getFileLink();
    }

    try (InputStream inputStream = new FileInputStream(reportSettings.getFile())) {
      MetaFile reportFile =
          Beans.get(MetaFiles.class)
              .upload(
                  inputStream, reportSettings.getOutputName() + "." + ReportSettings.FORMAT_DOCX);
      return "ws/rest/com.axelor.meta.db.MetaFile/"
          + reportFile.getId()
          + "/content/download?v="
          + reportFile.getVersion();
    }
  }
}
