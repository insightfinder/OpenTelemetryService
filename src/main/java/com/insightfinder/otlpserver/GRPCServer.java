package com.insightfinder.otlpserver;

import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.processor.LogExtractionProcessor;
import com.insightfinder.otlpserver.service.GrpcTraceService;
import com.insightfinder.otlpserver.service.GrpcLogService;
import com.insightfinder.otlpserver.util.RuleUtil;
import io.grpc.*;
import io.grpc.Context;
import org.slf4j.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GRPCServer{

  private static final Logger LOG = LoggerFactory.getLogger(GRPCServer.class);

  public static final Context.Key<Metadata> METADATA_KEY = Context.key("metadata");

  public static final ConcurrentLinkedQueue<LogData> logProcessQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<LogData> logSendQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<SpanData> spanProcessQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<SpanData> spanSendQueue = new ConcurrentLinkedQueue<>();

  public static void main(String[] args) throws Exception {

    // Startup
    LOG.info("Woring Dir: " + System.getProperty("user.dir"));
    LOG.info("ServerSettings:");
    LOG.info(Config.getServerConfig().toString());
    LOG.info("DataSettings:");
    LOG.info(Config.getDataConfig().toString());

    // Services
    GrpcTraceService traceService = new GrpcTraceService();
    GrpcLogService logService = new GrpcLogService();

    // Rule Engine
    RuleUtil.loadRules();

    // Interception
    GrpcInterceptionService interceptor = new GrpcInterceptionService();

    Server server = ServerBuilder.forPort(Config.getServerConfig().port)
      .addService(traceService)
      .addService(logService)
      .intercept(interceptor)
      .maxInboundMessageSize(16 * 1024 * 1024) // Add the interceptor
      .build();

    System.out.println("Starting OTLP Trace Receiver...");
    server.start();
    System.out.println("OTLP Trace Receiver started at port " + Config.getServerConfig().port);


    // Consumers

    ExecutorService executorService = Executors.newFixedThreadPool(Config.getServerConfig().worker.processThreads);
    for (int i = 0; i < Config.getServerConfig().worker.processThreads; i++) {
      executorService.submit(new LogExtractionProcessor(i));
    }


    server.awaitTermination();
  }

  static class GrpcInterceptionService  implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
      Context context = Context.current().withValue(METADATA_KEY, metadata);
      return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
  }

}
