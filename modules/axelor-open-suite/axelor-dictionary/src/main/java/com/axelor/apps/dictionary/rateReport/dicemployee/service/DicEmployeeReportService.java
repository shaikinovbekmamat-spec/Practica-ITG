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
package com.axelor.apps.dictionary.rateReport.dicemployee.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.dictionary.db.DicEmployee;
import com.axelor.apps.dictionary.rateReport.dicemployee.dto.DicEmployeeReportData;
import com.axelor.apps.dictionary.report.IDictionaryReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DicEmployeeReportService {

  private final ObjectMapper mapper;

  public DicEmployeeReportService() {
    this.mapper = new ObjectMapper();

    SimpleModule module = new SimpleModule();
    module.addSerializer(byte[].class, new ByteArrayToBase64Serializer());
    this.mapper.registerModule(module);
  }

  private static class ByteArrayToBase64Serializer extends StdSerializer<byte[]> {
    public ByteArrayToBase64Serializer() {
      super(byte[].class);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value != null && value.length > 0) {
        String base64String = Base64.getEncoder().encodeToString(value);
        gen.writeString(base64String);
        log.debug(
            "Serialized {} bytes to Base64 string (length: {})",
            value.length,
            base64String.length());
      } else {
        gen.writeNull();
      }
    }
  }

  public String generateEmployeeReport(DicEmployee employee, String format) throws Exception {
    DicEmployeeReportData data = DicEmployeeReportData.from(employee);
    String jsonData = mapper.writeValueAsString(data);

    log.info("Generated JSON data (length: {})", jsonData.length());

    if (data.getImageBytes() != null) {
      log.info("Image bytes length in DTO: {}", data.getImageBytes().length);
      if (jsonData.contains("\"imageBytes\":\"")) {
        log.info("Image bytes successfully serialized as Base64 string");
      } else {
        log.warn("Image bytes may not be serialized correctly - check JSON output");
      }
    } else {
      log.warn("Image bytes are null in DTO");
    }

    ReportSettings settings =
        ReportFactory.createReport(IDictionaryReport.EMPLOYEE_REPORT, "Employee-${date}")
            .addFormat(format)
            .addParam("JsonData", jsonData)
            .addParam("__locale", ReportSettings.getPrintingLocale())
            .generate();

    String fileName = settings.getOutputName();
    java.io.File file = settings.getFile();

    if (file != null && format != null) {
      java.nio.file.Path src = file.toPath();
      java.nio.file.Path dest =
          java.nio.file.Files.move(
              src,
              src.resolveSibling(fileName + "." + format),
              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      file = dest.toFile();
    }

    return com.axelor.utils.helpers.file.PdfHelper.getFileLinkFromPdfFile(file, fileName);
  }
}
