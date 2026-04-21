package com.axelor.apps.dictionary.Integration.dto;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class NbkrCurrencyDto {

  @XmlAttribute(name = "ISOCode")
  private String code;

  @XmlElement(name = "Nominal")
  private int nominal;

  @XmlElement(name = "Value")
  @XmlJavaTypeAdapter(CommaDecimalAdapter.class)
  private BigDecimal rate;

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public int getNominal() { return nominal; }
  public void setNominal(int nominal) { this.nominal = nominal; }

  public BigDecimal getRate() { return rate; }
  public void setRate(BigDecimal rate) { this.rate = rate; }

  /**
   * Адаптер для корректной обработки запятых в числах (например, "86,0000" -> 86.0000).
   */
  public static class CommaDecimalAdapter extends XmlAdapter<String, BigDecimal> {
    @Override
    public BigDecimal unmarshal(String v) {
      return v == null ? null : new BigDecimal(v.replace(",", "."));
    }
    @Override
    public String marshal(BigDecimal v) {
      return v == null ? null : v.toString();
    }
  }
}
