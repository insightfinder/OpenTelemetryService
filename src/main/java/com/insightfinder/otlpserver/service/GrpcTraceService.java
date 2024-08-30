package com.insightfinder.otlpserver.service;

import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.ParseUtil;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

import static com.insightfinder.otlpserver.GRPCServer.METADATA_KEY;
import static com.insightfinder.otlpserver.GRPCServer.traceProcessQueue;

public class GrpcTraceService extends TraceServiceGrpc.TraceServiceImplBase {

  private static final Logger LOG = LoggerFactory.getLogger(GrpcTraceService.class);

  @Override
  public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {

    // Extract trace data body
    exportSpanData(request);

    // Send a response back to the client
    ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  private void exportSpanData(ExportTraceServiceRequest request){
    Metadata metadata = METADATA_KEY.get();
    for(ResourceSpans resourceSpans: request.getResourceSpansList()){
      // Get the attributes at Trace Level
      var traceAttributes = ParseUtil.parseAttributes(resourceSpans.getResource().getAttributesList());

      for(ScopeSpans scopeSpans: resourceSpans.getScopeSpansList()){
        for(Span rawSpan: scopeSpans.getSpansList()){
          // Get the attributes at Span Level
          var span = new SpanData();
          span.traceID =  ParseUtil.parseHexadecimalBytes(rawSpan.getTraceId());
          span.traceAttributes = traceAttributes;
          span.duration = rawSpan.getEndTimeUnixNano() - rawSpan.getStartTimeUnixNano();
          span.spanID = ParseUtil.parseHexadecimalBytes(rawSpan.getSpanId());
          span.operationName = rawSpan.getName();
          span.startTime = rawSpan.getStartTimeUnixNano();
          span.endTime = rawSpan.getEndTimeUnixNano();
          span.spanAttributes = ParseUtil.parseAttributes(rawSpan.getAttributesList());
          span.metadata = metadata;
          traceProcessQueue.offer(span);
        }
      }
    }
  }
}
