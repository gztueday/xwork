package com.bigdata.xwork.query;

/**
 * Created by zouyi on 2018/3/9.
 */
public class SchedulerStartObject {
    private String schedulerName;
    private String paramsJson;

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }
}
