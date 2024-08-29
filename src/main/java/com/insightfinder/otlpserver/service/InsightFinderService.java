package com.insightfinder.otlpserver.service;
import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.IFLogDataPayload;
import com.insightfinder.otlpserver.entity.IFLogDataReceivePayload;
import com.insightfinder.otlpserver.entity.LogData;
import io.grpc.Metadata;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.*;


public class InsightFinderService {

  private static Logger LOG = LoggerFactory.getLogger(InsightFinderService.class);
  private static OkHttpClient httpClient = new OkHttpClient();
  private static final String LOG_STREAM_API = "/api/v1/customprojectrawdata";

  public static void createProjectIfNotExist(String projectName, String user ,String licenseKey) {
    MultivaluedHashMap<String, String> bodyJson = new MultivaluedHashMap<>();

    HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(Config.getServerConfig().insightFinderUrl+"/api/v1/check-and-add-custom-project")).newBuilder();
    //urlBuilder.addQueryParameter("userName","maoyuwang");
    //urlBuilder.addQueryParameter("licenseKey",licenseKey + 123);
    urlBuilder.addQueryParameter("projectName",projectName);
    urlBuilder.addQueryParameter("operation","check");
    urlBuilder.addQueryParameter("systemName","maoyu-test-otlp-system");

    String url = urlBuilder.build().toString();

//    RequestBody body = new FormBody.Builder()
//            .add("userName", user)
//            .add("licenseKey", licenseKey+"123")
//            .add("projectName",projectName)
//            .add("operation","check")
//            .add("systemName","maoyu-test-otlp-system")
//            .build();

    Request request = new Request.Builder()
            .url(url)
//            .addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
            .build();

    Response response;
    try{
      response = httpClient.newCall(request).execute();
      LOG.info(response.body().toString());
    }catch (Exception e){
      LOG.error(e.toString());
    }
  }

  public static void sendLogData(LogData logData){
    var iFLogData = new IFLogDataPayload();
    var payload = new IFLogDataReceivePayload();

    var user = logData.metadata.get(Metadata.Key.of("ifuser", Metadata.ASCII_STRING_MARSHALLER));
    var licenseKey =  logData.metadata.get(Metadata.Key.of("iflicenseKey", Metadata.ASCII_STRING_MARSHALLER));


    iFLogData.setData(logData);
    iFLogData.setTag(logData.instanceName);
    iFLogData.setTimeStamp(logData.timestamp);
    if(logData.componentName != null && !logData.componentName.isEmpty()){
      iFLogData.setComponentName(logData.componentName);
    }

    payload.setLogDataList(new ArrayList<>(List.of(iFLogData)));
    payload.setUserName(user);
    payload.setLicenseKey(licenseKey);
    payload.setProjectName(logData.projectName);
    payload.setInsightAgentType("LogStreaming");

    RequestBody body = RequestBody.create(JSON.toJSONBytes(payload), MediaType.get("application/json"));
    Request request = new Request.Builder()
            .url(Config.getServerConfig().insightFinderUrl + LOG_STREAM_API)
            .addHeader("agent-type", "Stream")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        System.err.println("Error sending log data: " + response.message());
      } else {
        // Get the response as a string
        String responseString = response.body().string();
        System.out.println("Response: " + responseString);
      }
    } catch (IOException e) {
      System.err.println("Error sending log data: " + e.getMessage());
    }


  }

  public static void main(String[] args) {
  }
}
