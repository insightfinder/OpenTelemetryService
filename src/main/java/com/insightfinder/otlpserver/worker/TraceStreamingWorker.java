package com.insightfinder.otlpserver.worker;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.service.InsightFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceStreamingWorker implements Runnable {

  private Logger LOG = LoggerFactory.getLogger(TraceStreamingWorker.class.getName());

  public TraceStreamingWorker(int threadNum){
    LOG.info("LogStreamingProcessor thread " + threadNum + " started.");
  }

  @Override
  public void run() {
    SpanData spanData;

    while(true){

      // Get task from the queue.
      try{
        spanData= GRPCServer.traceSendQueue.poll();
        if (spanData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        e.printStackTrace();
        continue;
      }
      InsightFinderService.sendTraceData(spanData);
    }
  }

}
