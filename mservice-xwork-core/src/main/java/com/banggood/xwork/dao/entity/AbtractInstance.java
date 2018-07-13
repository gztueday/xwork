package com.banggood.xwork.dao.entity;

import java.sql.Timestamp;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.banggood.xwork.action.core.WorkRunStatus;

public abstract class AbtractInstance extends AbstractInfo {

    /**
     * 运行实例ID
     */
    protected String instanceid;

    /**
     * 运行状态
     */
    protected WorkRunStatus status;
    /**
     * 对应的workFlowInstance的id
     */
    protected String wfInstanceid;

    /**
     * 运行参数
     */
    protected String runParamJSON;

    /**
     * 业务运行时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    protected Timestamp runTime;

    /**
     * 节点运行开始时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    protected Timestamp startTime;

    /**
     * 节点运行结束时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    protected Timestamp endTime;

    public String getWfInstanceid() {
        return wfInstanceid;
    }

    public void setWfInstanceid(String wfInstanceid) {
        this.wfInstanceid = wfInstanceid;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public WorkRunStatus getStatus() {
        return status;
    }

    public void setStatus(WorkRunStatus status) {
        this.status = status;
    }


    public String getRunParamJSON() {
        return runParamJSON;
    }

    public void setRunParamJSON(String runParamJSON) {
        this.runParamJSON = runParamJSON;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getRunTime() {
        return runTime;
    }

    public void setRunTime(Timestamp runTime) {
        this.runTime = runTime;
    }

}
