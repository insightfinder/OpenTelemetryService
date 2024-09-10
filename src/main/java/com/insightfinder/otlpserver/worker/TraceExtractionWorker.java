package com.insightfinder.otlpserver.worker;

import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.ParseUtil;
import com.insightfinder.otlpserver.util.RuleUtil;
import com.insightfinder.otlpserver.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.insightfinder.otlpserver.GRPCServer.traceStreamingQueue;


public class TraceExtractionWorker implements Runnable {

    public Logger LOG = LoggerFactory.getLogger(TraceExtractionWorker.class);

    public TraceExtractionWorker(int threadNum) {
        LOG.info("TraceExtractionProcessor thread " + threadNum + " started.");
    }


    @Override
    public void run() {
        SpanData spanData;

        while (true) {

            // Get the log data from the queue
            try {
                spanData = GRPCServer.traceProcessQueue.poll();
                if (spanData == null) {
                    Thread.sleep(100);
                    continue;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
                continue;
            }


            // Validate Data
            if (!ValidationUtil.ValidSpanData(spanData)) {
                continue;
            }

            // Setup
            var user = ParseUtil.getIfUserFromMetadata(spanData.metadata);

            // Extract projectName name
            var projectName = RuleUtil.extractSpanDataByRules(user, "project", spanData);
            if (projectName.isEmpty()) {
                LOG.warn("Unable to extract projectName.");
                LOG.warn(JSON.toJSONString(spanData));
                continue;
            } else {
                spanData.projectName = projectName;
            }

            // Extract instanceName
            var instanceName = RuleUtil.extractSpanDataByRules(user, "instance", spanData);
            if (instanceName.isEmpty()) {
                LOG.warn("Unable to extract instanceName.");
                LOG.warn(JSON.toJSONString(spanData));
                continue;
            } else {
                spanData.instanceName = instanceName;
            }

            // Extract ComponentName
            String componentName = RuleUtil.extractSpanDataByRules(user, "component", spanData);
            if (!componentName.isEmpty()) {
                spanData.componentName = componentName;
            }

            // Extract SystemName
            String systemName = RuleUtil.extractSpanDataByRules(user, "system", spanData);
            if (!systemName.isEmpty()) {
                spanData.systemName = systemName;
            }

            traceStreamingQueue.offer(spanData);
        }
    }
}
