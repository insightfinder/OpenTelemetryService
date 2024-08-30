package com.insightfinder.otlpserver.service;

import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.IFLogDataPayload;
import com.insightfinder.otlpserver.entity.IFLogDataReceivePayload;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.ParseUtil;
import com.insightfinder.otlpserver.util.TimestampUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class InsightFinderService {

    private static final Logger LOG = LoggerFactory.getLogger(InsightFinderService.class);
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final String LOG_STREAM_API = "/api/v1/customprojectrawdata";
    private static final String CHECK_PROJECT_API = "/api/v1/check-and-add-custom-project";

    public static boolean createProjectIfNotExist(String projectName, String projectType, String systemName, String user, String licenseKey) {

        boolean projectExist;
        RequestBody emptyFormBody = new FormBody.Builder()
                .add("anything", "anything")
                .build();

        // Check Project
        var checkProjectUrl = Objects.requireNonNull(HttpUrl.parse(Config.getServerConfig().insightFinderUrl + CHECK_PROJECT_API))
                .newBuilder()
                .addQueryParameter("userName", user)
                .addQueryParameter("licenseKey", licenseKey)
                .addQueryParameter("projectName", projectName)
                .addQueryParameter("systemName", systemName)
                .addQueryParameter("operation", "check").build();
        var checkProjectRequest = new Request.Builder().url(checkProjectUrl).addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(emptyFormBody).build();
        try (Response response = httpClient.newCall(checkProjectRequest).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    var responseBodyJson = JSON.parseObject(response.body().string());
                    projectExist = responseBodyJson.getBoolean("isProjectExist");
                } else {
                    projectExist = false;
                }

            } else {
                LOG.error("Request failed with code: {}", response.code());
                return false;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return false;
        }

        // Create Project
        if (!projectExist) {

            var createProjectUrl = Objects.requireNonNull(HttpUrl.parse("https://stg.insightfinder.com/api/v1/check-and-add-custom-project"))
                    .newBuilder()
                    .addQueryParameter("userName", user)
                    .addQueryParameter("licenseKey", licenseKey)
                    .addQueryParameter("projectName", projectName)
                    .addQueryParameter("systemName", systemName)
                    .addQueryParameter("instanceType", "PrivateCloud")
                    .addQueryParameter("projectCloudType", projectType)
                    .addQueryParameter("insightAgentType", "Custom")
                    .addQueryParameter("dataType", "Log")
                    .addQueryParameter("operation", "create").build();
            var createProjectRequest = new Request.Builder().url(createProjectUrl).addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(emptyFormBody).build();

            try (Response response = httpClient.newCall(createProjectRequest).execute()) {
                var responseBodyJson = JSON.parseObject(response.body().string());
                if (response.isSuccessful()) {
                    var isProjectCreated = responseBodyJson.getBoolean("success");
                    if (isProjectCreated) {
                        LOG.info("Project '{}' created for user '{}'", projectName, user);
                        return true;
                    } else {
                        LOG.error("Failed to create project '{}' for user '{}': {}", projectExist, user, responseBodyJson.getString("message"));
                        return false;
                    }
                } else {
                    LOG.error("Failed to create project '{}' for user '{}': {}", projectExist, user, response.code());
                    return false;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
                return false;
            }
        } else {
            return true;
        }
    }

    public static void sendData(Object data, String userName, String licenseKey, String projectName, String instanceName, long timestamp, String componentName) {
        var iFLogData = new IFLogDataPayload();
        var iFPayload = new IFLogDataReceivePayload();

        iFLogData.setData(data);
        iFLogData.setTimeStamp(timestamp);
        iFLogData.setTag(instanceName);
        if (componentName != null && !componentName.isEmpty()) {
            iFLogData.setComponentName(componentName);
        }
        iFPayload.setLogDataList(new ArrayList<>(List.of(iFLogData)));
        iFPayload.setUserName(userName);
        iFPayload.setLicenseKey(licenseKey);
        iFPayload.setProjectName(projectName);
        iFPayload.setInsightAgentType("LogStreaming");

        RequestBody body = RequestBody.create(JSON.toJSONBytes(iFPayload), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(Config.getServerConfig().insightFinderUrl + LOG_STREAM_API)
                .addHeader("agent-type", "Stream")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOG.error("Error sending log data with response: {}", response.message());
            }
        } catch (IOException e) {
            LOG.error("Error sending log data with exception: {}", e.getMessage());
        }
    }

    public static void sendLogData(LogData logData) {
        var user = ParseUtil.getIfUserFromMetadata(logData.metadata);
        var licenseKey = ParseUtil.getLicenseKeyFromMedata(logData.metadata);
        sendData(logData.data, user, licenseKey, logData.projectName, logData.instanceName, logData.timestamp, logData.componentName);
    }

    public static void sendTraceData(SpanData spanData) {
        var user = ParseUtil.getIfUserFromMetadata(spanData.metadata);
        var licenseKey = ParseUtil.getLicenseKeyFromMedata(spanData.metadata);
        sendData(spanData, user, licenseKey, spanData.projectName, spanData.instanceName, TimestampUtil.ToUnixMili(String.valueOf(spanData.startTime)), spanData.componentName);
    }
}
