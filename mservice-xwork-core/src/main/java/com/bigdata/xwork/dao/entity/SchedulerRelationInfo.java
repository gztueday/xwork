package com.bigdata.xwork.dao.entity;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zouyi on 2018/1/15.
 */
public class SchedulerRelationInfo {

    private String remoteWorkFlow;
    private String remoteAction;
    private String dependWorkFlow;
    public String dependActionStr;
    private List<String> dependActions;
    private String remoteCron;
    //    private String dependInstanceid;
    private String dependCron;
    private String schedulerInstanceid;
    //    private String remoteInstanceid;
    private int versions;
    //    private String remoteIp;
    private String relationid;
    //    private String dependIp;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRelationid() {
        return relationid;
    }

    public void setRelationid(String relationid) {
        this.relationid = relationid;
    }

    public String getRemoteWorkFlow() {
        return remoteWorkFlow;
    }

    public void setRemoteWorkFlow(String remoteWorkFlow) {
        this.remoteWorkFlow = remoteWorkFlow;
    }

    public String getRemoteAction() {
        return remoteAction;
    }

    public void setRemoteAction(String remoteAction) {
        this.remoteAction = remoteAction;
    }

    public String getDependWorkFlow() {
        return dependWorkFlow;
    }

    public void setDependWorkFlow(String dependWorkFlow) {
        this.dependWorkFlow = dependWorkFlow;
    }

    public String getRemoteCron() {
        return remoteCron;
    }

    public void setRemoteCron(String remoteCron) {
        this.remoteCron = remoteCron;
    }

    public String getDependCron() {
        return dependCron;
    }

    public void setDependCron(String dependCron) {
        this.dependCron = dependCron;
    }

    public String getSchedulerInstanceid() {
        return schedulerInstanceid;
    }

    public void setSchedulerInstanceid(String schedulerInstanceid) {
        this.schedulerInstanceid = schedulerInstanceid;
    }

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }

    public String getDependActionStr() {
        return dependActionStr;
    }

    public void setDependActionStr(String dependActionStr) {
        this.dependActionStr = dependActionStr;
    }

    public List<String> getDependActions() {
        return dependActions;
    }

    public void setDependActions(List<String> dependActions) {
        this.dependActions = dependActions;
    }

    public List<String> parseDependStr() {
        List<String> das = Arrays.asList(this.dependActionStr.split(","));
        this.dependActions = das;
        return das;
    }

}
