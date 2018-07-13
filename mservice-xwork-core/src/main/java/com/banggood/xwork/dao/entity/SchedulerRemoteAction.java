package com.banggood.xwork.dao.entity;

/**
 * Created by zouyi on 2018/3/16.
 */

/**
 * remote是发送请求方
 * depend是接收请求方
 */
public class SchedulerRemoteAction {
    private String wfInstance;
    private String dependSchedulerInstanceid;
    private long dependSchedulerInstanceVersion;
    private String remoteAction;
    private String dependAction;
    private String dependWorkFlowName;
    private long calculate;

    public long getCalculate() {
        return calculate;
    }

    public void setCalculate(long calculate) {
        this.calculate = calculate;
    }

    public String getDependWorkFlowName() {
        return dependWorkFlowName;
    }

    public void setDependWorkFlowName(String dependWorkFlowName) {
        this.dependWorkFlowName = dependWorkFlowName;
    }

    public String getDependAction() {
        return dependAction;
    }

    public void setDependAction(String dependAction) {
        this.dependAction = dependAction;
    }

    public String getRemoteAction() {
        return remoteAction;
    }

    public void setRemoteAction(String remoteAction) {
        this.remoteAction = remoteAction;
    }

    public String getWfInstance() {
        return wfInstance;
    }

    public void setWfInstance(String wfInstance) {
        this.wfInstance = wfInstance;
    }

    public String getDependSchedulerInstanceid() {
        return dependSchedulerInstanceid;
    }

    public void setDependSchedulerInstanceid(String dependSchedulerInstanceid) {
        this.dependSchedulerInstanceid = dependSchedulerInstanceid;
    }

    public long getDependSchedulerInstanceVersion() {
        return dependSchedulerInstanceVersion;
    }

    public void setDependSchedulerInstanceVersion(long dependSchedulerInstanceVersion) {
        this.dependSchedulerInstanceVersion = dependSchedulerInstanceVersion;
    }
}
