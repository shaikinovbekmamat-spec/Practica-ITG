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
package com.axelor.apps.dictionary.rateReport.helper;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.db.Repository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

public class ReportHelper {

  private static final ObjectMapper mapper = new ObjectMapper();

  /** Универсальный метод загрузки сущностей по ID или всех разом */
  public static <T extends Model> List<T> fetchByIds(Repository<T> repo, List<Integer> ids) {
    if (ObjectUtils.isEmpty(ids)) {
      return repo.all().fetch();
    }
    return ids.stream().map(id -> repo.find(id.longValue())).collect(Collectors.toList());
  }

  /** Универсальный запуск отчета из контроллера */
  public static void executeReport(
      ActionRequest request,
      ActionResponse response,
      String titleKey,
      String reportFile,
      DataSupplier dataSupplier) {
    try {
      @SuppressWarnings("unchecked")
      List<Integer> ids = (List<Integer>) request.getContext().get("_ids");
      Object data = dataSupplier.get(ids);
      String jsonData = mapper.writeValueAsString(data);

      String title = I18n.get(titleKey);
      String fileLink =
          ReportFactory.createReport(reportFile, title + "-${date}")
              .addFormat(ReportSettings.FORMAT_PDF)
              .addParam("DATA_SOURCE", jsonData)
              .addParam("__locale", ReportSettings.getPrintingLocale())
              .generate()
              .getFileLink();

      response.setView(ActionView.define(title).add("html", fileLink).map());

    } catch (Exception e) {
      response.setError(e.getMessage());
    }
  }

  @FunctionalInterface
  public interface DataSupplier {
    Object get(List<Integer> ids) throws JsonProcessingException, AxelorException;
  }
}
