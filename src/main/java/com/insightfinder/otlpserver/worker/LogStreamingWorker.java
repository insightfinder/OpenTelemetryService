package com.insightfinder.otlpserver.worker;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.service.InsightFinderService;
import com.insightfinder.otlpserver.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStreamingWorker implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(LogStreamingWorker.class);

  public LogStreamingWorker(int threadNum){
    LOG.info("LogStreamingProcessor thread " + threadNum + " started.");
  }

  @Override
  public void run() {
    LogData logData;

    while(true){

      // Get task from the queue.
      try{
        logData= GRPCServer.logStreamingQueue.poll();
        if (logData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        LOG.error(e.getMessage());
        continue;
      }

      var user = ParseUtil.getIfUserFromMetadata(logData.metadata);
      var licenseKey = ParseUtil.getLicenseKeyFromMedata(logData.metadata);

      // Create Project if not in cache list
      if (GRPCServer.projectLocalCache.get(logData.projectName) == null){
        var isProjectCreated = InsightFinderService.createProjectIfNotExist(logData.projectName,"Trace", logData.systemName,user,licenseKey);

        // Save creation result to cache
        if (isProjectCreated){
          GRPCServer.projectLocalCache.putIfAbsent(logData.projectName,true);
        }
      }

      InsightFinderService.sendLogData(logData);
      LOG.info("Send 1 log message to project '{}' for user '{}'",logData.projectName,user);
    }
  }

}
