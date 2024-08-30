package com.insightfinder.otlpserver.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import io.grpc.Metadata;

import java.util.HashMap;
import java.util.Map;

public class SpanData {

  public transient String componentName;
  public transient String instanceName;
  public transient String projectName;
  public transient String systemName;
  public transient Metadata metadata;

  @JSONField(name = "traceID")
  public String traceID;

  @JSONField(name = "traceAttributes")
  public HashMap<String,Object> traceAttributes;

  @JSONField(name = "spanID")
  public String spanID;

  @JSONField(name = "operationName")
  public String operationName;

  @JSONField(name = "startTime")
  public long startTime;

  @JSONField(name = "endTime")
  public long endTime;

  @JSONField(name = "duration")
  public long duration;

  @JSONField(name = "spanAttributes")
  public Map<String, Object> spanAttributes;



}



