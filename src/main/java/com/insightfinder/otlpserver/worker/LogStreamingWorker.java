package com.insightfinder.otlpserver.worker;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.service.InsightFinderService;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStreamingWorker implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(LogStreamingWorker.class.getName());

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
        e.printStackTrace();
        continue;
      }

      InsightFinderService.sendLogData(logData);
    }
  }

}
