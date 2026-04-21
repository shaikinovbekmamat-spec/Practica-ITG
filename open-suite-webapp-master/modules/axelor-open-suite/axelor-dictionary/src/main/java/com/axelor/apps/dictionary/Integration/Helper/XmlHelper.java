package com.axelor.apps.dictionary.Integration.Helper;

import java.io.StringReader;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;


public class XmlHelper {

  @SuppressWarnings("unchecked")
  public static <T> T unmarshal(String xml, Class<T> clazz) throws Exception {
    JAXBContext context = JAXBContext.newInstance(clazz);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    try (StringReader reader = new StringReader(xml)) {
      return (T) unmarshaller.unmarshal(reader);
    }
  }
}
