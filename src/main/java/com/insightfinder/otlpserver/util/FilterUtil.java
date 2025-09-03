package com.insightfinder.otlpserver.util;

import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.service.datafilter.LogDataFilter;

public class FilterUtil {
  private static final LogDataFilter LOG_DATA_FILTER = LogDataFilter.getInstance();

  public static void filter(String user, LogData rawData) {
    LOG_DATA_FILTER.filter(user, rawData);
  }
}
