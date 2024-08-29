package com.insightfinder.otlpserver.util;

public class TimestampUtil {
  public static long ToUnixMili(String origin){
    var length =  origin.length();
    if(length == 13){
      return Long.parseLong(origin);
    } else if(length > 13){
      return Long.parseLong(origin.substring(0,13));
    }
    else{
      var diffLen = 13 - length;
      return Long.parseLong(origin + "0".repeat(diffLen));
    }
  }
}
