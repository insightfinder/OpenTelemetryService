package com.insightfinder.otlpserver.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
  public static Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

  public static String getValueFromJsonStr(String path,String rawData){
    var jsonObject = JSON.parseObject(rawData);
    if(jsonObject == null){
      return null;
    }

    String[] keys = path.split("\\.");
    Object element = jsonObject;

    for (String key : keys) {
      if (!(element instanceof JSONObject)) {
        return null;
      }
      element = ((JSONObject) element).get(key);
    }

    return element != null && !(element instanceof JSONObject) ? element.toString() : null;
  }
}
