package com.bigdata.xwork.query;

/**
 * Created by zouyi on 2018/3/15.
 */
public class SchedulerStatusInstanceQueryObject extends QueryObject {

    protected Integer pageSize = 7;
    private String schedulerInstance;

    public String getSchedulerInstance() {
        return schedulerInstance;
    }

    public void setSchedulerInstance(String schedulerInstance) {
        this.schedulerInstance = schedulerInstance;
    }
}
