package com.insightfinder.otlpserver.util;

import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.config.DataConfig;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class RuleUtil {
    private static Logger LOG = LoggerFactory.getLogger(RuleUtil.class);
    public static Map<String, Map<String, List<Rule>>> logExtractionRules = new HashMap<>();
    public static Map<String, Map<String, List<Rule>>> traceExtractionRules = new HashMap<>();

    public static class Rule {
        public String source;
        public String field;
        public Pattern regex;
        public String value;
    }

    public static void initLogExtractionRules() {
        for (var user : Config.getDataConfig().users.keySet()) {
            logExtractionRules.putIfAbsent(user, new HashMap<>());

          if(Config.getDataConfig().users.get(user).log == null){
            LOG.warn("User %s doesn't have log extraction rules.".formatted(user));
            return;
          }

            // Load Log Rules
            var userLogExtractionRules = logExtractionRules.get(user);
            userLogExtractionRules.put("instance", CompileRules(Config.getDataConfig().users.get(user).log.extraction.instanceFrom));
            userLogExtractionRules.put("component", CompileRules(Config.getDataConfig().users.get(user).log.extraction.componentFrom));
            userLogExtractionRules.put("project", CompileRules(Config.getDataConfig().users.get(user).log.extraction.projectFrom));
            userLogExtractionRules.put("timestamp", CompileRules(Config.getDataConfig().users.get(user).log.extraction.timestampFrom));
            userLogExtractionRules.put("system", CompileRules(Config.getDataConfig().users.get(user).log.extraction.systemFrom));
        }
    }

    public static void initTraceExtractionRules() {
        for (var user : Config.getDataConfig().users.keySet()) {
            traceExtractionRules.putIfAbsent(user, new HashMap<>());

            if(Config.getDataConfig().users.get(user).trace == null){
              LOG.warn("User %s doesn't have trace extraction rules.".formatted(user));
              return;
            }

            // Load Trace Rules
            var userLogExtractionRules = traceExtractionRules.get(user);
            userLogExtractionRules.put("instance", CompileRules(Config.getDataConfig().users.get(user).trace.extraction.instanceFrom));
            userLogExtractionRules.put("component", CompileRules(Config.getDataConfig().users.get(user).trace.extraction.componentFrom));
            userLogExtractionRules.put("project", CompileRules(Config.getDataConfig().users.get(user).trace.extraction.projectFrom));
            userLogExtractionRules.put("system", CompileRules(Config.getDataConfig().users.get(user).trace.extraction.systemFrom));
        }
    }

    public static List<Rule> CompileRules(List<DataConfig.ExtractionRuleStr> ruleList) {
        var result = new ArrayList<Rule>();
        for (DataConfig.ExtractionRuleStr ruleStr : ruleList) {
            var rule = new Rule();
            rule.source = ruleStr.source;
            rule.field = ruleStr.field;
            rule.value = ruleStr.value;
            if (ruleStr.regex != null) {
                rule.regex = Pattern.compile(ruleStr.regex);
            }
            result.add(rule);
        }
        return result;
    }

    public static String extractLogDataByRules(String user, String dataType, LogData logData) {
        var result = "";
        for (var rule : logExtractionRules.get(user).get(dataType)) {
            if (rule.source.equals("body")) {
                String bodyValue = JsonUtil.getValueFromJsonStr(rule.field, logData.rawData);
                if (bodyValue != null) {
                    var matchResult = rule.regex.matcher(bodyValue);
                    if (matchResult.find()) {
                        result = matchResult.group();
                        break;
                    }
                }
            } else if (rule.source.equals("header")) {
                String headerValue = logData.metadata.get(Metadata.Key.of(rule.field, Metadata.ASCII_STRING_MARSHALLER));
                if (headerValue != null) {
                    var matchResult = rule.regex.matcher(headerValue);
                    if (matchResult.find()) {
                        result = matchResult.group();
                        break;
                    }
                }
            } else if (rule.source.equals("static")) {
                result = rule.value;
            }
        }
        return result;
    }


    public static String extractSpanDataByRules(String user, String dataType, SpanData spanData) {
        var result = "";
        // Try traceAttributes
        String bodyString;
        Object bodyObject;
        String subField;
        for (var rule : traceExtractionRules.get(user).get(dataType)) {
            if (rule.source.equals("body")) {
                if (rule.field.startsWith("traceAttributes")) {
                    subField = rule.field.replaceFirst("traceAttributes.", "");
                    bodyObject = spanData.traceAttributes.get(subField);

                } else {
                    subField = rule.field.replaceFirst("spanAttributes.", "");
                    bodyObject = spanData.spanAttributes.get(subField);
                }
                if (bodyObject == null) {
                    continue;
                }
                if (!(bodyObject instanceof String)) {
                    continue;
                }
                bodyString = bodyObject.toString();
                var matchResult = rule.regex.matcher(bodyString);
                if (matchResult.find()) {
                    result = matchResult.group();
                    return result;
                }


            } else if (rule.source.equals("header")) {
                String headerValue = spanData.metadata.get(Metadata.Key.of(rule.field, Metadata.ASCII_STRING_MARSHALLER));
                if (headerValue != null) {
                    var matchResult = rule.regex.matcher(headerValue);
                    if (matchResult.find()) {
                        result = matchResult.group();
                        return result;
                    }
                }
            } else if (rule.source.equals("static")) {
                result = rule.value;
                return result;
            }
        }
        return result;
    }

    public static long extractLogTimestampByRules(String user, LogData logData) {
        long result = 0;
        for (var rule : logExtractionRules.get(user).get("timestamp")) {
            if (rule.source.equals("body")) {
                String bodyValue = JsonUtil.getValueFromJsonStr(rule.field, logData.rawData);
                if (bodyValue != null) {
                    result = TimestampUtil.ToUnixMili(bodyValue);
                }
            } else if (rule.source.equals("sender")) {
                String headerValue = logData.metadata.get(Metadata.Key.of(rule.field, Metadata.ASCII_STRING_MARSHALLER));
                if (headerValue != null) {
                    result = TimestampUtil.ToUnixMili(headerValue);
                }
            }
        }
        return result;
    }

}
