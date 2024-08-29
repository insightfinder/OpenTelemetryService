package com.insightfinder.otlpserver.entity;

import com.google.gson.annotations.SerializedName;
import io.grpc.Metadata;

import java.util.HashMap;
import java.util.Map;

public class SpanData {

  public long timestamp;
  public String componentName;
  public String instanceName;
  public String projectName;

  public Metadata metadata;

  @SerializedName("traceID")
  public String traceID;

  @SerializedName("traceAttributes")
  public HashMap<String,Object> traceAttributes;

  @SerializedName("spanID")
  public String spanID;

  @SerializedName("operationName")
  public String operationName;

  @SerializedName("startTime")
  public long startTime;

  @SerializedName("endTime")
  public long endTime;

  @SerializedName("duration")
  public long duration;

  @SerializedName("spanAttributes")
  public Map<String, Object> spanAttributes;



}



