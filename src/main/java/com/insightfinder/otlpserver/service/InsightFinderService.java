package com.insightfinder.otlpserver.service;

import com.alibaba.fastjson2.JSON;
import com.insightfinder.otlpserver.config.Config;
import com.insightfinder.otlpserver.entity.IFLogDataPayload;
import com.insightfinder.otlpserver.entity.IFLogDataReceivePayload;
import com.insightfinder.otlpserver.entity.LogData;
import com.insightfinder.otlpserver.entity.SpanData;
import com.insightfinder.otlpserver.util.ParseUtil;
import com.insightfinder.otlpserver.util.TimestampUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class InsightFinderService {

    private final Logger LOG = LoggerFactory.getLogger(InsightFinderService.class);
    private final String LOG_STREAM_API = "/api/v1/customprojectrawdata";
    private final String CHECK_PROJECT_API = "/api/v1/check-and-add-custom-project";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String ifUrl;

    public InsightFinderService(String ifUrl) {
        this.ifUrl = ifUrl;
    }

    public boolean createProjectIfNotExist(String projectName, String projectType, String systemName, String user, String licenseKey) {

        boolean projectExist;
        var emptyFormBody = "anything=anything";

        // Check Project
        var checkProjectUrl = URI.create(Config.getServerConfig().insightFinderUrl + CHECK_PROJECT_API + 
                "?userName=" + user + 
                "&licenseKey=" + licenseKey + 
                "&projectName=" + projectName + 
                "&systemName=" + systemName + 
                "&operation=check");
        var checkProjectRequest = HttpRequest.newBuilder()
                .uri(checkProjectUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(emptyFormBody))
                .build();
        try {
            var response = httpClient.send(checkProjectRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var responseBodyJson = JSON.parseObject(response.body());
                projectExist = responseBodyJson.getBoolean("isProjectExist");
            } else {
                LOG.error("Request failed with code: {}", response.statusCode());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            LOG.error(e.getMessage());
            return false;
        }

        // Create Project
        if (!projectExist) {
            var createProjectUrl = URI.create(ifUrl + CHECK_PROJECT_API + 
                    "?userName=" + user + 
                    "&licenseKey=" + licenseKey + 
                    "&projectName=" + projectName + 
                    "&systemName=" + systemName + 
                    "&instanceType=PrivateCloud" + 
                    "&projectCloudType=" + projectType + 
                    "&insightAgentType=Custom" + 
                    "&dataType=Log" + 
                    "&operation=create");
            var createProjectRequest = HttpRequest.newBuilder()
                    .uri(createProjectUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(emptyFormBody))
                    .build();

            try {
                var response = httpClient.send(createProjectRequest, HttpResponse.BodyHandlers.ofString());
                var responseBodyJson = JSON.parseObject(response.body());
                if (response.statusCode() == 200) {
                    var isProjectCreated = responseBodyJson.getBoolean("success");
                    if (isProjectCreated) {
                        LOG.info("Project '{}' created for user '{}'", projectName, user);
                        return true;
                    } else {
                        LOG.error("Failed to create project '{}' for user '{}': {}", projectExist, user, responseBodyJson.getString("message"));
                        return false;
                    }
                } else {
                    LOG.error("Failed to create project '{}' for user '{}': {}", projectExist, user, response.statusCode());
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                LOG.error(e.getMessage());
                return false;
            }
        } else {
            return true;
        }
    }

    public void sendData(Object data, String userName, String licenseKey, String projectName, String instanceName, long timestamp, String componentName) {
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

        var body = JSON.toJSONBytes(iFPayload);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(Config.getServerConfig().insightFinderUrl + LOG_STREAM_API))
                .header("agent-type", "Stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.error("Error sending log data with response: {}", response.body());
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Error sending log data with exception: {}", e.getMessage());
        }
    }

    public void sendData(LogData logData) {
        var user = ParseUtil.getIfUserFromMetadata(logData.metadata);
        var licenseKey = ParseUtil.getLicenseKeyFromMedata(logData.metadata);
        sendData(logData.data, user, licenseKey, logData.projectName, logData.instanceName, logData.timestamp, logData.componentName);
    }

    public void sendData(SpanData spanData) {
        var user = ParseUtil.getIfUserFromMetadata(spanData.metadata);
        var licenseKey = ParseUtil.getLicenseKeyFromMedata(spanData.metadata);
        sendData(spanData, user, licenseKey, spanData.projectName, spanData.instanceName, TimestampUtil.ToUnixMili(String.valueOf(spanData.startTime)), spanData.componentName);
    }
}