package com.insightfinder.otlpserver.entity;

import com.google.gson.annotations.SerializedName;
import io.grpc.Metadata;

public class LogData {
  @SerializedName("spanId")
  public String spanId;

  @SerializedName("traceId")
  public String traceId;

  @SerializedName("data")
  public String data;

  public long timestamp;
  public String componentName;
  public String instanceName;
  public String projectName;
  public long senderTimestamp;


  public Metadata metadata;
}
