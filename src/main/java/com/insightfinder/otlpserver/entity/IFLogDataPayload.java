package com.insightfinder.otlpserver.entity;

import com.google.gson.annotations.SerializedName;

public class IFLogDataPayload {

    @SerializedName("timestamp")
    private long timeStamp;

    @SerializedName("tag")
    private String tag;

    @SerializedName("data")
    private Object data;

    @SerializedName("componentName")
    private String componentName;

    // Getters and Setters
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
