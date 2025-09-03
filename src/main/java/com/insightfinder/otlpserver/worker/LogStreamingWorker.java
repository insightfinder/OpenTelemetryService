package com.insightfinder.otlpserver.worker;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.service.InsightFinderService;
import com.insightfinder.otlpserver.util.ParseUtil;
import com.insightfinder.otlpserver.util.ValidationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStreamingWorker implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(LogStreamingWorker.class);
  private InsightFinderService InsightFinder = new InsightFinderService(Config.getServerConfig().insightFinderUrl);
  private HashMap<String, ArrayList<LogData>> batchCache = new HashMap<>();
  private long lastSendTime = System.currentTimeMillis();


  public LogStreamingWorker(int threadNum){
    LOG.info("LogStreamingProcessor thread " + threadNum + " started.");
  }


  private void sendDataIfExpired(){
    // Send batched Data if lastSendDurationInMili is more than 1min
    var lastSendDurationInMili = (System.currentTimeMillis() - lastSendTime);
    if(lastSendDurationInMili >= 60000){
      for(var entry : batchCache.entrySet() ){
        var entryValue = entry.getValue();
        if (entryValue.size() != 0){
          InsightFinder.sendLogData(entryValue);
          lastSendTime = System.currentTimeMillis();
          entryValue.clear();
          LOG.info("Send {} log messages to project '{}' due to expiring.",entryValue.size(),entry.getKey());
        }
      }
    }
  }

  @Override
  public void run() {
    LogData logData;

    while(true){

      // Get task from the queue.
      try{
        logData= GRPCServer.logStreamingQueue.poll();
        if (logData == null){
          sendDataIfExpired();
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        LOG.error(e.getMessage());
        continue;
      }

      LOG.info("=== LOG STREAMING WORKER PROCESSING ===");
      LOG.info("Processing LogData: projectName={}, instanceName={}, rawData='{}'", 
               logData.projectName, logData.instanceName, logData.rawData);
      LOG.info("LogData data field: {}", logData.data);

      var user = ParseUtil.getIfUserFromMetadata(logData.metadata);
      var licenseKey = ParseUtil.getLicenseKeyFromMedata(logData.metadata);

      LOG.info("User: {}, LicenseKey: {}", user, licenseKey);

      if (!ValidationUtil.ValidLogData(logData)) {
        LOG.warn("Log data validation failed in streaming worker.");
        continue;
      }

      LOG.info("LogData validation passed in streaming worker");

      // Create Project if not in cache list
      if (GRPCServer.projectLocalCache.get(logData.projectName) == null){
        var isProjectCreated = InsightFinder.createProjectIfNotExist(logData.projectName,"Log", logData.systemName,user,licenseKey);

        // Save creation result to cache
        if (isProjectCreated){
          GRPCServer.projectLocalCache.putIfAbsent(logData.projectName,true);
        }
      }

      // Init batchCache
      if(batchCache.get(logData.projectName)==null){
        batchCache.put(logData.projectName, new ArrayList<>());
      }


      // Add the data to the batchCache
      ArrayList<LogData> batchCacheSlot = batchCache.get(logData.projectName);
      batchCacheSlot.add(logData);

      // Send the data and clear the batchCache if we got more than $streamingBatchSize messages.
      var batchCacheSlotSize = batchCacheSlot.size();
      if(batchCacheSlotSize >= Config.getServerConfig().worker.streamingBatchSize){
        InsightFinder.sendLogData(batchCacheSlot);
        lastSendTime = System.currentTimeMillis();
        batchCacheSlot.clear();
        LOG.info("Send {} log messages to project '{}' for user '{}'",batchCacheSlotSize,logData.projectName,user);
      }
      

    }
  }

}
