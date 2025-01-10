package com.insightfinder.otlpserver.service.datafilter;

import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.config.JsonStructure;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.util.JsonUtil;
import com.insightfinder.otlpserver.util.RuleUtil;
import com.insightfinder.otlpserver.util.RuleUtil.FilterRule;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class LogDataFilter {

  private static LogDataFilter instance;

  private LogDataFilter() {
  }

  public static LogDataFilter getInstance() {
    if (instance == null) {
      instance = new LogDataFilter();
    }
    return instance;
  }

  public void filter(String user, LogData data) {
    List<FilterRule> filterRules = getFilterRule(user);
    if (filterRules == null || filterRules.isEmpty()) {
      return;
    }
    for (FilterRule filterRule : filterRules) {
      filter(filterRule, data);
    }
  }

  private void filter(FilterRule filterRule, LogData data) {
    if (JsonUtil.isValidJsonStr(data.rawData)) {
      List<JsonStructure> jsonStructures = filterRule.jsonStructures;
      if (jsonStructures == null || jsonStructures.isEmpty()) {
        return;
      }
      var jsonObj = JSON.parseObject(data.rawData);
      for (JsonStructure jsonStructure : jsonStructures) {
        jsonStructure.removeFromObject(jsonObj);
      }
      data.rawData = jsonObj.toJSONString();
    } else {
      if (filterRule.regexPatterns == null || filterRule.regexPatterns.isEmpty()) {
        return;
      }
      for (Pattern pattern : filterRule.regexPatterns) {
        String sensitiveStr = getMatchedGroupedString(data.rawData, pattern);
        data.rawData = data.rawData.replace(sensitiveStr, StringUtils.EMPTY);
      }
    }
  }

  private List<FilterRule> getFilterRule(String user) {
    try {
      return RuleUtil.logFilterRules.get(user);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Given a string and the regex expression find one matched substring
   *
   * @param string
   * @param pattern
   * @return
   */
  private String getMatchedGroupedString(String string, Pattern pattern) {
    if (pattern == null) {
      return null;
    }
    Matcher matcher = pattern.matcher(string);
    if (matcher.find()) {
      String extractedValue = null;
      if (matcher.groupCount() == 1) {
        extractedValue = matcher.group(1);
      }
      if (StringUtils.isEmpty(extractedValue)) {
        extractedValue = matcher.group();
      }
      return extractedValue;
    }
    return StringUtils.EMPTY;
  }

}
