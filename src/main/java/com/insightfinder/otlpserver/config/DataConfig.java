package com.insightfinder.otlpserver.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;



public class DataConfig {
  public Map<String, User> users;

  public static class User {
    public String licenseKey;
    public dataType trace;
    public dataType log;
  }

  public static class dataType {
    public ExtractionItems extraction;
    public Map<String, FilterRuleStr> filter;
  }

  public static class ExtractionItems {
    public List<ExtractionRuleStr> projectFrom;
    public List<ExtractionRuleStr> instanceFrom;
    public List<ExtractionRuleStr> componentFrom;
    public List<ExtractionRuleStr> timestampFrom;
    public List<ExtractionRuleStr> systemFrom;
  }

  public static class FilterRuleStr {
    public List<String> jsonFieldName;
    public List<String> stringContent;
  }

  public static class ExtractionRuleStr {
    public String source;
    public String field;
    public String regex;
    public String value;
  }

  @Override
  public String toString() {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
    Yaml yaml = new Yaml(options);
    return yaml.dump(this);
  }

}
