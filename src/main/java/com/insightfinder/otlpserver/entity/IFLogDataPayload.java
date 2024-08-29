package com.insightfinder.otlpserver.entity;

import com.alibaba.fastjson2.annotation.JSONField;

public class IFLogDataPayload {

    @JSONField(name = "timestamp")
    private long timeStamp;

    @JSONField(name = "tag")
    private String tag;

    @JSONField(name = "data")
    private Object data;

    @JSONField(name = "componentName")
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
