package com.bigdata.xwork.dao.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by zouyi on 2018/3/9.
 */
public class Bundle {
    /**
     * 远程依赖的scheduler实例的版本
     */
    private long calculate;

    /**
     * 与远程实例开始关联开始的时间
     */
    private Date startTime;

    /**
     * 关系绑定与配置相关联
     */
    private String schedulerName;
    /**
     * 远程依赖的scheduler实例的id
     */
    private String remoteInstanceid;
    private String remoteActionStr;
    private List<String> remoteActions;

    private String dependAction;

    private String reqObj;

    /**
     * 开始依赖的第一个版本
     */
    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<String> getRemoteActions() {
        return remoteActions;
    }

    public void setRemoteActions(List<String> remoteActions) {
        this.remoteActions = remoteActions;
    }

    public String getRemoteActionStr() {
        return remoteActionStr;
    }

    public void setRemoteActionStr(String remoteActionStr) {
        this.remoteActionStr = remoteActionStr;
    }

    public String getDependAction() {
        return dependAction;
    }

    public void setDependAction(String dependAction) {
        this.dependAction = dependAction;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public long getCalculate() {
        return calculate;
    }

    public void setCalculate(long calculate) {
        this.calculate = calculate;
    }

    public String getRemoteInstanceid() {
        return remoteInstanceid;
    }

    public void setRemoteInstanceid(String remoteInstanceid) {
        this.remoteInstanceid = remoteInstanceid;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getReqObj() {
        return reqObj;
    }

    public void setReqObj(String reqObj) {
        this.reqObj = reqObj;
    }

}
