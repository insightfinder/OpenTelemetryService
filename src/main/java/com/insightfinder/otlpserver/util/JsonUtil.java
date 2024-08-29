package com.insightfinder.otlpserver.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtil {

  public static JsonElement parseJson(String rawString){
    JsonElement result;
    try{
      result = JsonParser.parseString(rawString);
    }catch (Exception e){
      return null;
    }
    return result;
  }

  public static String getValueFromJsonStr(String path,String rawData){
    JsonElement element;
    try{
      element = JsonParser.parseString(rawData).getAsJsonObject();
    }catch (Exception e){
      return null;
    }


    for (String key : path.split("\\.")) {
      if (element == null || !element.isJsonObject()) {
        return null;
      }
      element = element.getAsJsonObject().get(key);
    }
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }
}
