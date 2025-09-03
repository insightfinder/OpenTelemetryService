package com.insightfinder.otlpserver.config;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class JsonStructure {

  private String[] keys;
  private String valueRegex;
  private String jsonPath;
  private transient Pattern valueRegexPattern;
  private static final String JSON_PATH_CONNECTOR = "->";
  private static final String JSON_KEY_VALUE_SPLITOR = "=";
  private static final int NO_VALUES = 1;
  private static final int HAS_VALUES = 2;

  public JsonStructure(String jsonStructureStr, boolean parsePattern) {
    String[] keyAndValue = splitKeyAndValue(jsonStructureStr);
    switch (keyAndValue.length) {
      case NO_VALUES:
        this.jsonPath = keyAndValue[0];
        this.keys = getKeys(jsonPath);
        break;
      case HAS_VALUES:
        this.jsonPath = keyAndValue[0];
        this.keys = getKeys(jsonPath);
        this.valueRegex = keyAndValue[1];
        if (parsePattern) {
          this.valueRegexPattern = getPattern(valueRegex, false);
        }
        break;
      default:
        break;
    }
  }

  private String[] getKeys(String keys) {
    return keys.split(JSON_PATH_CONNECTOR);
  }

  public String[] getKeys() {
    return keys;
  }

  public void setKeys(String[] keys) {
    this.keys = keys;
  }

  public String getValueRegex() {
    return valueRegex;
  }

  public void setValueRegex(String valueRegex) {
    this.valueRegex = valueRegex;
  }

  public String getJsonPath() {
    return jsonPath;
  }

  public void setJsonPath(String jsonPath) {
    this.jsonPath = jsonPath;
  }

  public boolean isNoValue() {
    return StringUtils.isEmpty(this.valueRegex);
  }

  public Pattern getValueRegexPattern() {
    return valueRegexPattern;
  }

  public void setValueRegexPattern(Pattern valueRegexPattern) {
    this.valueRegexPattern = valueRegexPattern;
  }

  public void removeFromObject(JSONObject object) {
    if (isNoValue()) {
      removeJsonValueFromObject(object, keys, 0);
    } else {
      removeJsonValueFromObjectByRegex(object, keys, 0, valueRegex);
    }
  }

  private static String[] splitKeyAndValue(String str) {
    if (StringUtils.isEmpty(str)) {
      return new String[0];
    }
    return str.split(JSON_KEY_VALUE_SPLITOR, 2);
  }

  private static Pattern getPattern(String regex, boolean multiLineFlag) {
    if (multiLineFlag) {
      return Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
    }
    return Pattern.compile(regex);
  }

  /**
   * Recursive function to remove the key by the given json path
   *
   * @param valueObject
   * @param keys
   * @param index       - the start index of the json key to search
   */
  private static void removeJsonValueFromObject(Object valueObject, String[] keys, int index) {
    for (int i = index; i < keys.length; i++) {
      String key = keys[i];
      if (valueObject instanceof JSONObject) {
        JSONObject obj = (JSONObject) valueObject;
        if (i == keys.length - 1 && obj.containsKey(key)) {
          obj.remove(key);
        } else if (obj.containsKey(key)) {
          valueObject = obj.get(key);
        } else {
          break;
        }
      } else if (valueObject instanceof JSONArray) {
        JSONArray array = (JSONArray) valueObject;
        for (int j = 0; j < array.size(); j++) {
          Object obj = array.get(j);
          if (i == keys.length - 1 && obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.containsKey(key)) {
              jsonObject.remove(key);
            }
          } else {
            removeJsonValueFromObject(obj, keys, i);
          }
        }
      } else {
        break;
      }
    }
  }

  /**
   * Recursive function to remove the specific sub string in the key by the given json path
   *
   * @param valueObject
   * @param keys
   * @param index       - the start index of the json key to search
   * @param valueRegex  - the regex to match the substring to remove
   */
  private static void removeJsonValueFromObjectByRegex(Object valueObject, String[] keys, int index,
      String valueRegex) {
    for (int i = index; i < keys.length; i++) {
      String key = keys[i];
      if (valueObject instanceof JSONObject) {
        JSONObject obj = (JSONObject) valueObject;
        if (i == keys.length - 1 && obj.containsKey(key)) {
          String value = obj.get(key).toString();
          String processedValue = removeStringByRegex(value, valueRegex);
          obj.put(key, processedValue);
        } else if (obj.containsKey(key)) {
          valueObject = obj.get(key);
        } else {
          break;
        }
      } else if (valueObject instanceof JSONArray) {
        JSONArray array = (JSONArray) valueObject;
        for (int j = 0; j < array.size(); j++) {
          Object obj = array.get(j);
          if (i == keys.length - 1 && obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.containsKey(key)) {
              String value = jsonObject.get(key).toString();
              String processedValue = removeStringByRegex(value, valueRegex);
              jsonObject.put(key, processedValue);
            }
          } else {
            removeJsonValueFromObject(obj, keys, i);
          }
        }
      } else {
        break;
      }
    }
  }

  private static String removeStringByRegex(String string, String regex) {
    return string.replaceAll(regex, StringUtils.EMPTY);
  }
}
