package com.insightfinder.otlpserver.worker;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.insightfinder.otlpserver.GRPCServer.logStreamingQueue;


public class LogExtractionWorker implements Runnable {

  private final Logger LOG = LoggerFactory.getLogger(LogExtractionWorker.class);

  public LogExtractionWorker(int threadNum){
    LOG.info("LogExtractionProcessor thread " + threadNum + " started.");
  }


  @Override
  public void run() {
    LogData logData;

    while(true) {

      // Get the log data from the queue
      try{
        logData= GRPCServer.logProcessQueue.poll();
        if (logData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        LOG.error(e.getMessage());
        continue;
      }

      LOG.info("=== LOG EXTRACTION WORKER PROCESSING ===");
      LOG.info("Processing LogData with rawData: '{}'", logData.rawData);
      LOG.info("Metadata: {}", logData.metadata);

      // Validate LogData
      if(!ValidationUtil.ValidLogData(logData)){
        LOG.warn("LogData validation failed for rawData: '{}'", logData.rawData);
        continue;
      }
      
      LOG.info("LogData validation passed");

      // Setup
      var user = ParseUtil.getIfUserFromMetadata(logData.metadata);

      // Extract projectName name
      var projectName = RuleUtil.extractLogDataByRules(user, "project", logData);
      if (projectName.isEmpty()){
        LOG.warn("No project name found.");
        continue;
      }else{
        logData.projectName = projectName;
      }

      // Extract instanceName
      var instanceName = RuleUtil.extractLogDataByRules(user, "instance", logData);
      if (instanceName.isEmpty()){
        continue;
      }else{
        logData.instanceName = instanceName;
      }

      // Extract timestamp
      long timestamp = RuleUtil.extractLogTimestampByRules(user, logData);
      if (timestamp == 0){
        continue;
      }else {
        logData.timestamp = timestamp;
      }

      // Extract ComponentName
      String componentName = RuleUtil.extractLogDataByRules(user, "component", logData);
      if (!componentName.isEmpty()){
        logData.componentName = componentName;
      }

      // Extract SystemName
      String systemName = RuleUtil.extractLogDataByRules(user, "system", logData);
      if (!systemName.isEmpty()){
        logData.systemName = systemName;
      }


      // Transform Raw data to actual data
      if(JsonUtil.isValidJsonStr(logData.rawData)){
        JSONObject jsonObject = JSON.parseObject(logData.rawData);
        logData.data = jsonObject;
      }else{
        logData.data = logData.rawData;
      }

      logData.rawData ="";

      logStreamingQueue.offer(logData);
    }
  }
}
