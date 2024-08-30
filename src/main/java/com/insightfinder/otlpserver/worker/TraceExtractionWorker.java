package com.insightfinder.otlpserver.worker;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.RuleUtil;
import com.insightfinder.otlpserver.util.ValidationUtil;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.insightfinder.otlpserver.GRPCServer.logStreamingQueue;
import static com.insightfinder.otlpserver.GRPCServer.traceSendQueue;


public class TraceExtractionWorker implements Runnable {

  public static Logger LOG = LoggerFactory.getLogger(TraceExtractionWorker.class.getName());

  public TraceExtractionWorker(int threadNum){
    LOG.info("TraceExtractionProcessor thread " + threadNum + " started.");
  }


  @Override
  public void run() {
    SpanData spanData = new SpanData();

    while(true) {

      // Get the log data from the queue
      try{
        spanData = GRPCServer.traceProcessQueue.poll();
        if (spanData == null){
          Thread.sleep(100);
          continue;
        }
      }catch (Exception e){
        e.printStackTrace();
        continue;
      }


      // Validate Data
      if(!ValidationUtil.ValidSpanData(spanData)){
        continue;
      }

      // Setup
      var user = spanData.metadata.get(Metadata.Key.of("ifuser", Metadata.ASCII_STRING_MARSHALLER));

      // Extract projectName name
      var projectName = RuleUtil.extractSpanDataByRules(user, "project", spanData);
      if (projectName.isEmpty()){
        continue;
      }else{
        spanData.projectName = projectName;
      }

      // Extract instanceName
      var instanceName = RuleUtil.extractSpanDataByRules(user, "instance", spanData);
      if (instanceName.isEmpty()){
        continue;
      }else{
        spanData.instanceName = instanceName;
      }

      // Extract ComponentName
      String componentName = RuleUtil.extractSpanDataByRules(user, "component", spanData);
      if (!componentName.isEmpty()){
        spanData.componentName = componentName;
      }

      // Extract SystemName
      String systemName = RuleUtil.extractSpanDataByRules(user, "system", spanData);
      if (!systemName.isEmpty()){
        spanData.systemName = systemName;
      }

      traceSendQueue.offer(spanData);
    }
  }
}
