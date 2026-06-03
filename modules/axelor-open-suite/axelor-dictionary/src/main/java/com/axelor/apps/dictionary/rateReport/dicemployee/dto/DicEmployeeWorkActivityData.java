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

import com.axelor.apps.dictionary.db.DicEmployeeWorkActivity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DicEmployeeWorkActivityData {

  private String introductionDate;
  private String careDate;
  private String nameOfInstitution;
  private String positionHeld;

  public static DicEmployeeWorkActivityData from(DicEmployeeWorkActivity workActivity) {
    if (workActivity == null) {
      return null;
    }
    return DicEmployeeWorkActivityData.builder()
        .introductionDate(
            workActivity.getIntroductionDate() != null
                ? String.valueOf(workActivity.getIntroductionDate())
                : null)
        .careDate(
            workActivity.getCareDate() != null ? String.valueOf(workActivity.getCareDate()) : null)
        .nameOfInstitution(workActivity.getNameOfInstitution())
        .positionHeld(workActivity.getPositionHeld())
        .build();
  }
}
