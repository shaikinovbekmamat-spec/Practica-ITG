package com.axelor.apps.dictionary.Integration.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "CurrencyRates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NbkrRatesDto {

  @XmlAttribute(name = "Date")
  @XmlJavaTypeAdapter(LocalDateAdapter.class)
  private LocalDate date;

  @XmlElement(name = "Currency")
  private List<NbkrCurrencyDto> currencies;

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }

  public List<NbkrCurrencyDto> getCurrencies() { return currencies; }
  public void setCurrencies(List<NbkrCurrencyDto> currencies) { this.currencies = currencies; }

  /**
   * Адаптер для формата даты НБКР (dd.MM.yyyy).
   */
  public static class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate unmarshal(String v) {
      return v == null ? null : LocalDate.parse(v, formatter);
    }
    @Override
    public String marshal(LocalDate v) {
      return v == null ? null : v.format(formatter);
    }
  }
}
