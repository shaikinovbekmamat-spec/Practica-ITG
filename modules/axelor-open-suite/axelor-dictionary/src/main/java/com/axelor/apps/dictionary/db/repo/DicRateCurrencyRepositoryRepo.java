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
package com.axelor.apps.dictionary.db.repo;

import com.axelor.apps.dictionary.actionlog.annotation.ActionLog;
import com.axelor.apps.dictionary.db.DicRateCurrency;
import com.axelor.i18n.I18n;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;

public class DicRateCurrencyRepositoryRepo extends DicRateCurrencyRepository {

  @Override
  @ActionLog(action = "DIC_RATE_CURRENCY_SAVE")
  public DicRateCurrency save(DicRateCurrency entity) {
    validate(entity);
    return super.save(entity);
  }

  @Override
  @ActionLog(action = "DIC_RATE_CURRENCY_DELETE")
  public void remove(DicRateCurrency entity) {
    super.remove(entity);
  }

  private void validate(DicRateCurrency entity) {
    if (entity == null) {
      throw new ValidationException(I18n.get("Exchange rate record is empty."));
    }

    if (entity.getRate() == null || entity.getRate().compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException(I18n.get("Exchange rate must be greater than 0."));
    }

    if (entity.getNominal() != null && entity.getNominal() <= 0) {
      throw new ValidationException(I18n.get("Nominal must be greater than 0."));
    }
  }
}
