package com.insightfinder.otlpserver.util;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.HashMap;
import java.util.List;

public class ParseUtil {
  public static Object parseAnyValue(AnyValue rawValue){
    return switch (rawValue.getValueCase()) {
      case INT_VALUE -> rawValue.getIntValue();
      case STRING_VALUE -> rawValue.getStringValue();
      case BOOL_VALUE -> rawValue.getBoolValue();
      case DOUBLE_VALUE -> rawValue.getDoubleValue();
      case ARRAY_VALUE -> rawValue.getArrayValue();
      case KVLIST_VALUE -> rawValue.getKvlistValue();
      case BYTES_VALUE -> rawValue.getBytesValue();
      default -> null;
    };
  }

  public static HashMap<String,Object> parseAttributes(List<KeyValue> attributes){
    var result = new HashMap<String,Object>();
    for (var attribute: attributes){
      result.put(attribute.getKey(), parseAnyValue(attribute.getValue()));
    }
    return result;
  }

  public static String parseHexadecimalBytes(ByteString byteString){
    var result = new StringBuilder();
    for (Byte b: byteString){
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }
}
