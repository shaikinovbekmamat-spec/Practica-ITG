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
package com.axelor.apps.dictionary.rateReport.dicemployee.dto;

import com.axelor.apps.dictionary.db.DicEmployee;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DicEmployeeReportData {

  private String address;
  private String birthDay;
  private String citizenship;
  private String education;
  private String fullName;
  private String graduated;
  private String nationality;
  private String disciplinarySanctions;
  private byte[] imageBytes;
  private List<DicEmployeeWorkActivityData> workActivities;

  public static DicEmployeeReportData from(DicEmployee employee) {
    if (employee == null) {
      return null;
    }

    MetaFile picture = employee.getImage();
    byte[] imageBytes = null;
    if (picture != null) {
      try {
        Path path = MetaFiles.getPath(picture);
        log.info(
            "Attempting to read image for employee {} from path: {}", employee.getFullName(), path);
        if (path != null && Files.exists(path)) {
          imageBytes = Files.readAllBytes(path);
          log.info(
              "Successfully read {} bytes for employee {}",
              imageBytes.length,
              employee.getFullName());
        } else {
          log.warn(
              "Image file not found at path: {} for employee {}", path, employee.getFullName());
        }
      } catch (Exception e) {
        log.error("Failed to read image bytes for employee {}", employee.getFullName(), e);
      }
    } else {
      log.info("No image assigned for employee {}", employee.getFullName());
    }

    return DicEmployeeReportData.builder()
        .imageBytes(imageBytes)
        .address(employee.getAddress())
        .birthDay(employee.getBirthDay() != null ? String.valueOf(employee.getBirthDay()) : null)
        .citizenship(employee.getCitizenship())
        .education(employee.getEducation())
        .fullName(employee.getFullName())
        .graduated(employee.getGraduated())
        .nationality(employee.getNationality())
        .disciplinarySanctions(employee.getDisciplinarySanctions())
        .workActivities(
            employee.getWorkActivities() != null
                ? employee.getWorkActivities().stream()
                    .map(DicEmployeeWorkActivityData::from)
                    .collect(Collectors.toList())
                : null)
        .build();
  }
}
