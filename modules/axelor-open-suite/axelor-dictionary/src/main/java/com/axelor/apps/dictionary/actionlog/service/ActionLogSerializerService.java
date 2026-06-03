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
package com.axelor.apps.dictionary.actionlog.service;

import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Singleton;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ActionLogSerializerService {

  private static final Logger log = LoggerFactory.getLogger(ActionLogSerializerService.class);

  private static final int MAX_SERIALIZATION_DEPTH = 3;

  private static final Set<String> EXCLUDED_PROPERTIES =
      Set.of("class", "selected", "version", "createdOn", "updatedOn", "createdBy", "updatedBy");

  private final ObjectMapper objectMapper;

  public ActionLogSerializerService() {
    this.objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public String extractRequestBody(Object[] arguments) {
    if (arguments == null || arguments.length == 0) {
      return "{}";
    }
    try {
      for (Object arg : arguments) {
        if (arg instanceof ActionRequest actionRequest) {
          Context context = actionRequest.getContext();
          return objectMapper.writeValueAsString(context);
        }
      }

      for (Object arg : arguments) {
        if (arg instanceof Model model) {
          return objectMapper.writeValueAsString(serializeModel(model, 0));
        }
      }

      for (Object arg : arguments) {
        if (arg == null || isSimpleValue(arg)) {
          continue;
        }
        return objectMapper.writeValueAsString(serializeValue(arg, 0));
      }
    } catch (Exception e) {
      log.warn("Could not serialize request context", e);
    }
    return "{}";
  }

  private Object serializeValue(Object value, int depth) {
    if (value == null || isSimpleValue(value)) {
      return value;
    }

    if (depth >= MAX_SERIALIZATION_DEPTH) {
      return String.valueOf(value);
    }

    if (value instanceof Model model) {
      return serializeModel(model, depth + 1);
    }

    if (value instanceof Map<?, ?> mapValue) {
      Map<String, Object> serializedMap = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
        serializedMap.put(
            String.valueOf(entry.getKey()), serializeValue(entry.getValue(), depth + 1));
      }
      return serializedMap;
    }

    if (value instanceof Collection<?> collectionValue) {
      List<Object> serializedCollection = new ArrayList<>();
      for (Object item : collectionValue) {
        serializedCollection.add(serializeValue(item, depth + 1));
      }
      return serializedCollection;
    }

    if (value.getClass().isArray()) {
      List<Object> serializedArray = new ArrayList<>();
      int length = Array.getLength(value);
      for (int i = 0; i < length; i++) {
        serializedArray.add(serializeValue(Array.get(value, i), depth + 1));
      }
      return serializedArray;
    }

    return String.valueOf(value);
  }

  private Map<String, Object> serializeModel(Model model, int depth) {
    Model entity = EntityHelper.getEntity(model);
    Map<String, Object> serialized = new LinkedHashMap<>();

    try {
      for (PropertyDescriptor propertyDescriptor :
          Introspector.getBeanInfo(EntityHelper.getEntityClass(entity), Object.class)
              .getPropertyDescriptors()) {

        String propertyName = propertyDescriptor.getName();
        if (EXCLUDED_PROPERTIES.contains(propertyName)) {
          continue;
        }

        Method readMethod = propertyDescriptor.getReadMethod();
        if (readMethod == null) {
          continue;
        }

        Object propertyValue = readMethod.invoke(entity);
        if (propertyValue == null) {
          continue;
        }

        serialized.put(propertyName, serializeValue(propertyValue, depth + 1));
      }
    } catch (Exception e) {
      log.warn("Could not introspect model {}", entity.getClass().getName(), e);
    }

    serialized.putIfAbsent("id", entity.getId());
    serialized.putIfAbsent("_entity", EntityHelper.getEntityClass(entity).getSimpleName());
    return serialized;
  }

  private boolean isSimpleValue(Object value) {
    return value instanceof CharSequence
        || value instanceof Number
        || value instanceof Boolean
        || value instanceof Enum<?>
        || value instanceof Temporal;
  }
}
