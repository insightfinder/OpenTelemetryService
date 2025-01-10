package com.insightfinder.otlpserver.service.datafilter;

import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.config.JsonStructure;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.util.JsonUtil;
import com.insightfinder.otlpserver.util.RuleUtil.FilterRule;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class SensitiveDataFilter extends Filter {

  private static SensitiveDataFilter instance;
  private static final String FILTER_NAME = "sensitive";

  private SensitiveDataFilter() {
  }

  public static SensitiveDataFilter getInstance() {
    if (instance == null) {
      instance = new SensitiveDataFilter();
    }
    return instance;
  }

  @Override
  public void filter(String user, LogData data) {
    FilterRule filterRule = getFilterRule(user);
    if (filterRule == null) {
      return;
    }
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

  @Override
  protected String getFilterName() {
    return FILTER_NAME;
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
