package com.insightfinder.otlpserver.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import io.grpc.Metadata;

public class LogData {

  @JSONField(name = "data")
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
