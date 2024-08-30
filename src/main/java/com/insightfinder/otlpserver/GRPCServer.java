package com.insightfinder.otlpserver;

import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.ValidationUtil;
import com.insightfinder.otlpserver.worker.LogExtractionWorker;
import com.insightfinder.otlpserver.service.GrpcTraceService;
import com.insightfinder.otlpserver.service.GrpcLogService;
import com.insightfinder.otlpserver.util.RuleUtil;
import com.insightfinder.otlpserver.worker.LogStreamingWorker;
import com.insightfinder.otlpserver.worker.TraceExtractionWorker;
import com.insightfinder.otlpserver.worker.TraceStreamingWorker;
import io.grpc.*;
import io.grpc.Context;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import org.slf4j.*;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GRPCServer{

  private static final Logger LOG = LoggerFactory.getLogger(GRPCServer.class);

  public static final Context.Key<Metadata> METADATA_KEY = Context.key("metadata");

  public static final ConcurrentLinkedQueue<LogData> logProcessQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<LogData> logStreamingQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<SpanData> traceProcessQueue = new ConcurrentLinkedQueue<>();
  public static final ConcurrentLinkedQueue<SpanData> traceSendQueue = new ConcurrentLinkedQueue<>();

  public static final ConcurrentHashMap<String,Boolean> projectLocalCache = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {

    // Startup
    LOG.info("Woring Dir: " + System.getProperty("user.dir"));
    LOG.info("ServerSettings:");
    LOG.info(Config.getServerConfig().toString());
    LOG.info("DataSettings:");
    LOG.info(Config.getDataConfig().toString());

    if(ValidationUtil.ValidateDataConfig()){
      LOG.info("Data configuration is valid.");
    }else{
      LOG.error("Exiting because data configuration is not valid.");
      System.exit(1);
    }

    // Services
    GrpcTraceService traceService = new GrpcTraceService();
    GrpcLogService logService = new GrpcLogService();

    // Rule Engine
    RuleUtil.initLogExtractionRules();
    RuleUtil.initTraceExtractionRules();

    // Interception
    GrpcInterceptionService interceptor = new GrpcInterceptionService();


    Server server;
    if(Config.getServerConfig().tls.enabled){

      SslContext sslContext = GrpcSslContexts.forServer(new File(Config.getServerConfig().tls.certificateFile), new File(Config.getServerConfig().tls.privateKeyFile))
              .trustManager(new File(Config.getServerConfig().tls.certificateFile))
              .build();

      server = NettyServerBuilder.forPort(Config.getServerConfig().port)
              .addService(traceService)
              .addService(logService)
              .intercept(interceptor) // Add the interceptor to store metadata
              .maxInboundMessageSize(16 * 1024 * 1024)
              .sslContext(sslContext)
              .build();

    }else {
      server = ServerBuilder.forPort(Config.getServerConfig().port)
              .addService(traceService)
              .addService(logService)
              .intercept(interceptor) // Add the interceptor to store metadata
              .maxInboundMessageSize(16 * 1024 * 1024)
              .build();
    }


    LOG.info("Starting OTLP Trace Receiver...");
    server.start();
    LOG.info("OTLP Trace Receiver started at port " + Config.getServerConfig().port);


    // LogExtraction Workers
    ExecutorService logExtractionWorkerPool = Executors.newFixedThreadPool(Config.getServerConfig().worker.processThreads);
    for (int i = 0; i < Config.getServerConfig().worker.processThreads; i++) {
      logExtractionWorkerPool.submit(new LogExtractionWorker(i));
    }

    // LogStreaming Workers
    ExecutorService logStreamingWorkerPool = Executors.newFixedThreadPool(Config.getServerConfig().worker.streamingThreads);
    for (int i = 0; i < Config.getServerConfig().worker.streamingThreads; i++) {
      logStreamingWorkerPool.submit(new LogStreamingWorker(i));
    }

    // TraceExtraction Workers
    ExecutorService traceExtractionWorkerPool = Executors.newFixedThreadPool(Config.getServerConfig().worker.processThreads);
    for (int i = 0; i < Config.getServerConfig().worker.processThreads; i++) {
      traceExtractionWorkerPool.submit(new TraceExtractionWorker(i));
    }

    // TraceStreaming Workers
    ExecutorService traceStreamingWorkerPool = Executors.newFixedThreadPool(Config.getServerConfig().worker.streamingThreads);
    for (int i = 0; i < Config.getServerConfig().worker.streamingThreads; i++) {
      traceStreamingWorkerPool.submit(new TraceStreamingWorker(i));
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
