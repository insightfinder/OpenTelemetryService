package com.insightfinder.otlpserver.entity;

import com.google.gson.annotations.SerializedName;
import io.grpc.Metadata;

public class LogData {
  @SerializedName("spanId")
  public String spanId;

  @SerializedName("traceId")
  public String traceId;

  @SerializedName("data")
  public Object data;


  // Fields not included in Serialization.
  public transient String rawData;
  public transient long timestamp;
  public transient String componentName;
  public transient String instanceName;
  public transient String systemName;
  public transient String projectName;
  public transient long senderTimestamp;
  public transient Metadata metadata;
}
