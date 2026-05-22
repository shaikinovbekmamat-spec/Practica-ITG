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
