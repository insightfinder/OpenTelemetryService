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
    exportLogData(request);

    // Send a response back to the client
    ExportLogsServiceResponse response = ExportLogsServiceResponse.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }


  public void exportLogData(ExportLogsServiceRequest request){
    Metadata metadata = METADATA_KEY.get();

    for(var resourceLog: request.getResourceLogsList()){
      for(var scopeLog: resourceLog.getScopeLogsList()){
        for(var logRecord: scopeLog.getLogRecordsList()){
          var logData = new LogData();
          logData.rawData = logRecord.getBody().getStringValue();
          logData.metadata = metadata;
          logData.senderTimestamp = logRecord.getTimeUnixNano();
          var result = GRPCServer.logProcessQueue.offer(logData);
          if (!result){
            LOG.warn("Unable to process log record: {} because queue is full", logRecord);
          }
        }
      }
    }
  }
}
