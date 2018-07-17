package com.bigdata.xwork.dao.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

public class WorkSchedulerInfo extends AbstractInfo {

    /**
     * cron表达式
     */
    private String cronStr;

    /**
     * 所调度的work action flow名称
     */
    private String flowName;

    /**
     * 调度名称
     */
    private String schedulerName;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    private Date createTime;

    private String configJson;

    private WorkFlowInfo workFlowInfo;

    public WorkFlowInfo getWorkFlowInfo() {
        return workFlowInfo;
    }

    public void setWorkFlowInfo(WorkFlowInfo workFlowInfo) {
        this.workFlowInfo = workFlowInfo;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public String getCronStr() {
        return cronStr;
    }

    public void setCronStr(String cronStr) {
        this.cronStr = cronStr;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "WorkSchedulerInfo{" +
                "cronStr='" + cronStr + '\'' +
                ", flowName='" + flowName + '\'' +
                ", schedulerName='" + schedulerName + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", configJson='" + configJson + '\'' +
                '}';
    }

}
