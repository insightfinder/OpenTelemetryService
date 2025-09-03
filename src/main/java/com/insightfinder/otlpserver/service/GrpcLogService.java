package com.insightfinder.otlpserver.service;

import com.insightfinder.otlpserver.GRPCServer;
import com.insightfinder.otlpserver.entity.LogData;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.insightfinder.otlpserver.GRPCServer.METADATA_KEY;

public class GrpcLogService extends LogsServiceGrpc.LogsServiceImplBase {

  private static final Logger LOG = LoggerFactory.getLogger(GrpcLogService.class);

  @Override
  public void export(ExportLogsServiceRequest request, StreamObserver<ExportLogsServiceResponse> responseObserver) {
    LOG.info("Received {} logs.",request.getResourceLogsCount());
    
    // Print raw log request for debugging
    LOG.info("=== RAW LOG REQUEST START ===");
    LOG.info("Full request: {}", request.toString());
    LOG.info("=== RAW LOG REQUEST END ===");
    
    exportLogData(request);

    // Send a response back to the client
    ExportLogsServiceResponse response = ExportLogsServiceResponse.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }


  public void exportLogData(ExportLogsServiceRequest request){
    Metadata metadata = METADATA_KEY.get();

    LOG.info("=== PROCESSING LOG DATA ===");
    LOG.info("Metadata received: {}", metadata);
    LOG.info("Metadata keys: {}", metadata.keys());
    LOG.info("Number of resource logs: {}", request.getResourceLogsCount());
    
    for(var resourceLog: request.getResourceLogsList()){
      LOG.info("Processing resource log with {} scope logs", resourceLog.getScopeLogsCount());
      LOG.info("Resource attributes: {}", resourceLog.getResource().getAttributesList());
      
      for(var scopeLog: resourceLog.getScopeLogsList()){
        LOG.info("Processing scope log with {} log records", scopeLog.getLogRecordsCount());
        LOG.info("Scope name: {}", scopeLog.getScope().getName());
        
        for(var logRecord: scopeLog.getLogRecordsList()){
          LOG.info("=== RAW LOG RECORD START ===");
          LOG.info("Log record body: {}", logRecord.getBody().getStringValue());
          LOG.info("Log record timestamp: {}", logRecord.getTimeUnixNano());
          LOG.info("Log record severity: {}", logRecord.getSeverityText());
          LOG.info("Log record attributes: {}", logRecord.getAttributesList());
          LOG.info("Full log record: {}", logRecord.toString());
          LOG.info("=== RAW LOG RECORD END ===");
          
          var logData = new LogData();
          logData.rawData = logRecord.getBody().getStringValue();
          logData.metadata = metadata;
          logData.senderTimestamp = logRecord.getTimeUnixNano();
          
          LOG.info("Created LogData with rawData: '{}', senderTimestamp: {}", logData.rawData, logData.senderTimestamp);
          
          var result = GRPCServer.logProcessQueue.offer(logData);
          if (!result){
            LOG.warn("Unable to process log record: {} because queue is full", logRecord);
          } else {
            LOG.info("Successfully added log data to processing queue");
          }
        }
      }
    }
    LOG.info("=== FINISHED PROCESSING LOG DATA ===");
  }
}
