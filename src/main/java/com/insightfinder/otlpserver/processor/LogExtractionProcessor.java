package com.insightfinder.otlpserver.processor;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.util.*;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.insightfinder.otlpserver.GRPCServer.logSendQueue;


public class LogExtractionProcessor implements Runnable {

  public static Logger LOG = LoggerFactory.getLogger(LogExtractionProcessor.class.getName());

  public LogExtractionProcessor(int threadNum){
    LOG.info("LogExtractionProcessor thread " + threadNum + " started.");
  }


  @Override
  public void run() {
    LogData logData = new LogData();

    while(true) {

      // Get the log data from the queue
      try{
        logData= GRPCServer.logProcessQueue.poll();
        if (logData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        e.printStackTrace();
      }


      // Validate LogData
      if(!ValidationUtil.ValidLogData(logData)){
        continue;
      }

      // Setup
      var user = logData.metadata.get(Metadata.Key.of("ifuser", Metadata.ASCII_STRING_MARSHALLER));

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
        LOG.warn("No instanceName found.");
        continue;
      }else{
        logData.instanceName = instanceName;
      }

      // Extract timestamp
      long timestamp = RuleUtil.extractLogTimestampByRules(user, logData);
      if (timestamp == 0){
        LOG.warn("No timestamp found.");
        continue;
      }else {
        logData.timestamp = timestamp;
      }

      // Extract ComponentName
      String componentName = RuleUtil.extractLogDataByRules(user, "component", logData);
      if (!componentName.isEmpty()){
        logData.componentName = componentName;
      }

      logSendQueue.offer(logData);
    }
  }
}
