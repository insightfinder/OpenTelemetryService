package com.insightfinder.otlpserver.service.datafilter;

import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.util.RuleUtil;
import com.insightfinder.otlpserver.util.RuleUtil.FilterRule;

public abstract class Filter {

  public abstract void filter(String user, LogData data);

  protected abstract String getFilterName();

  protected FilterRule getFilterRule(String user) {
    try {
      return RuleUtil.logFilterRules.get(user).get(getFilterName());
    } catch (Exception e) {
      return null;
    }
  }
}
