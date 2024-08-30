package com.insightfinder.otlpserver.worker;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.service.InsightFinderService;
import com.insightfinder.otlpserver.util.ParseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceStreamingWorker implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(TraceStreamingWorker.class);

  public TraceStreamingWorker(int threadNum){
    LOG.info("LogStreamingProcessor thread " + threadNum + " started.");
  }

  @Override
  public void run() {
    SpanData spanData;

    while(true){

      // Get task from the queue.
      try{
        spanData= GRPCServer.traceStreamingQueue.poll();
        if (spanData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        LOG.error(e.getMessage());
        continue;
      }


      var user = ParseUtil.getIfUserFromMetadata(spanData.metadata);
      var licenseKey =  ParseUtil.getLicenseKeyFromMedata(spanData.metadata);

      // Create Project if not in cache list
      if (GRPCServer.projectLocalCache.get(spanData.projectName) == null){
        var isProjectCreated = InsightFinderService.createProjectIfNotExist(spanData.projectName,"Trace", spanData.systemName,user,licenseKey);

        // Save creation result to cache
        if (isProjectCreated){
          GRPCServer.projectLocalCache.putIfAbsent(spanData.projectName,true);
        }
      }

      InsightFinderService.sendTraceData(spanData);
      LOG.info("Send 1 trace message to project '{}' for user '{}'",spanData.projectName,user);
    }
  }

}
