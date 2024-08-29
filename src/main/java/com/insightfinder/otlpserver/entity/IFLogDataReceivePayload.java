package com.insightfinder.otlpserver.entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class IFLogDataReceivePayload {

    @SerializedName("userName")
    private String userName;

    @SerializedName("projectName")
    private String projectName;

    @SerializedName("licenseKey")
    private String licenseKey;

    @SerializedName("metricData")
    private List<IFLogDataPayload> logDataList;

    @SerializedName("agentType")
    private String insightAgentType;

    @SerializedName("minTimestamp")
    private Long minTimestamp;

    @SerializedName("maxTimestamp")
    private Long maxTimestamp;

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public List<IFLogDataPayload> getLogDataList() {
        return logDataList;
    }

    public void setLogDataList(List<IFLogDataPayload> logDataList) {
        this.logDataList = logDataList;
    }

    public String getInsightAgentType() {
        return insightAgentType;
    }

    public void setInsightAgentType(String insightAgentType) {
        this.insightAgentType = insightAgentType;
    }

    public Long getMinTimestamp() {
        return minTimestamp;
    }

    public void setMinTimestamp(Long minTimestamp) {
        this.minTimestamp = minTimestamp;
    }

    public Long getMaxTimestamp() {
        return maxTimestamp;
    }

    public void setMaxTimestamp(Long maxTimestamp) {
        this.maxTimestamp = maxTimestamp;
    }
}
