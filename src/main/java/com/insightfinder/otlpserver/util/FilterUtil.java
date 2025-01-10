package com.insightfinder.otlpserver.util;

import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.service.datafilter.SensitiveDataFilter;

public class FilterUtil {
  private static final SensitiveDataFilter sensitiveDataFilter = SensitiveDataFilter.getInstance();

  public static void filter(String user, LogData rawData) {
    sensitiveDataFilter.filter(user, rawData);
  }
}
