package com.insightfinder.otlpserver.entity;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

public class IFLogDataReceivePayload {

    @JSONField(name = "userName")
    private String userName;

    @JSONField(name = "projectName")
    private String projectName;

    @JSONField(name = "licenseKey")
    private String licenseKey;

    @JSONField(name = "metricData")
    private List<IFLogDataPayload> logDataList;

    @JSONField(name = "agentType")
    private String insightAgentType;

    @JSONField(name = "minTimestamp")
    private Long minTimestamp;

    @JSONField(name = "maxTimestamp")
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
