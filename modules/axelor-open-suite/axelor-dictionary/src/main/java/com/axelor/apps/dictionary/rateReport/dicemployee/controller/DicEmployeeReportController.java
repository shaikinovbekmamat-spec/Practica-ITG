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
package com.axelor.apps.dictionary.rateReport.dicemployee.controller;

import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.db.DicEmployee;
import com.axelor.apps.dictionary.db.repo.DicEmployeeRepository;
import com.axelor.apps.dictionary.rateReport.dicemployee.service.DicEmployeeReportService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class DicEmployeeReportController {

  private final DicEmployeeRepository dicEmployeeRepository;
  private final DicEmployeeReportService dicEmployeeReportService;

  @Inject
  public DicEmployeeReportController(
      DicEmployeeRepository dicEmployeeRepository,
      DicEmployeeReportService dicEmployeeReportService) {
    this.dicEmployeeRepository = dicEmployeeRepository;
    this.dicEmployeeReportService = dicEmployeeReportService;
  }

  @ActionLog
  public void printReport(ActionRequest request, ActionResponse response) {
    Long employeeId = getEmployeeId(request.getContext());

    if (employeeId == null) {
      response.setError("Please save the employee first.");
      return;
    }

    generateEmployeeReport(response, employeeId, ReportSettings.FORMAT_PDF, false);
  }

  public void downloadReportPdf(ActionRequest request, ActionResponse response) {
    Long employeeId = getEmployeeId(request.getContext());

    if (employeeId == null) {
      response.setError("Please save the employee first.");
      return;
    }

    generateEmployeeReport(response, employeeId, ReportSettings.FORMAT_PDF, true);
  }

  public void downloadReportWord(ActionRequest request, ActionResponse response) {
    Long employeeId = getEmployeeId(request.getContext());

    if (employeeId == null) {
      response.setError("Please save the employee first.");
      return;
    }

    generateEmployeeReport(response, employeeId, ReportSettings.FORMAT_DOCX, true);
  }

  private void generateEmployeeReport(
      ActionResponse response, Long employeeId, String format, boolean download) {
    DicEmployee employee = dicEmployeeRepository.find(employeeId);
    if (employee == null) {
      response.setError("Employee not found.");
      return;
    }

    try {
      String fileLink = dicEmployeeReportService.generateEmployeeReport(employee, format);
      ActionViewBuilder viewBuilder = ActionView.define("Employee Report").add("html", fileLink);
      if (download || !ReportSettings.FORMAT_PDF.equalsIgnoreCase(format)) {
        viewBuilder.param("download", "true");
      }
      response.setView(viewBuilder.map());
    } catch (Exception e) {
      response.setError("Error generating report: " + e.getMessage());
    }
  }

  private Long getEmployeeId(Context context) {
    Object employeeIdObj = context.get("_employeeId");
    Object employeeIdsObj = context.get("_employeeIds");

    if (employeeIdObj == null
        && employeeIdsObj instanceof List<?> employeeIds
        && !employeeIds.isEmpty()) {
      employeeIdObj = employeeIds.get(0);
    }

    if (employeeIdObj == null) {
      employeeIdObj = context.get("id");
    }
    if (employeeIdObj == null) {
      employeeIdObj = context.get("_id");
    }
    if (employeeIdObj == null) {
      Object idsObj = context.get("_ids");
      if (idsObj instanceof List<?> ids && !ids.isEmpty()) {
        employeeIdObj = ids.get(0);
      }
    }
    if (employeeIdObj == null) {
      DicEmployee employee = context.asType(DicEmployee.class);
      if (employee != null) {
        return employee.getId();
      }
      return null;
    }
    return Long.valueOf(employeeIdObj.toString());
  }
}
