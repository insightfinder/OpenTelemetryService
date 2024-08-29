package com.insightfinder.otlpserver.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtil {
  public static String getValueFromJsonStr(String path,String rawData){
    JsonElement element = JsonParser.parseString(rawData).getAsJsonObject();

    for (String key : path.split("\\.")) {
      if (element == null || !element.isJsonObject()) {
        return null;
      }
      element = element.getAsJsonObject().get(key);
    }
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }
}
