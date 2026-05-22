package com.axelor.apps.dictionary.actionlog.service;

import jakarta.inject.Singleton;

@Singleton
public class ActionLogExceptionService {

  public String getStackTraceAsString(Throwable e) {
    if (e == null) return "";
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element).append("\n");
    }
    return sb.toString();
  }
}
