package com.bigdata.xwork.dao.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * work flow 运行实例
 */
public class WorkFlowInstance extends AbtractInstance {

    /**
     * 调度ID,通过调度ID关联两个work action flow,这样便可以让两个work action flow的工作频率不一致
     */
    private String schedulerid;

    /**
     * 运行实例名称
     */
    private String flowName;

    /**
     * 实例化后的工作流依赖关系
     */
    private String workFlowInfoJSON;

    private boolean delete;

    /**
     * 实例所在IP
     */
    private String executeIP;

    private String fatherInstanceId;

    private int versions;

    private String runningParams;

    private String subWorkFlowJson;

    public String getSubWorkFlowJson() {
        return subWorkFlowJson;
    }

    public void setSubWorkFlowJson(String subWorkFlowJson) {
        this.subWorkFlowJson = subWorkFlowJson;
    }

    public String getRunningParams() {
        return runningParams;
    }

    public void setRunningParams(String runningParams) {
        this.runningParams = runningParams;
    }

    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }

    private List<WorkActionInstance> workActionInstances = new ArrayList<>();

    public List<WorkActionInstance> getWorkActionInstances() {
        return workActionInstances;
    }

    public String getFatherInstanceId() {
        return fatherInstanceId;
    }

    public void setFatherInstanceId(String fatherInstanceId) {
        this.fatherInstanceId = fatherInstanceId;
    }

    public void setWorkActionInstances(List<WorkActionInstance> workActionInstances) {
        this.workActionInstances = workActionInstances;
    }

    public String getSchedulerid() {
        return schedulerid;
    }

    public void setSchedulerid(String schedulerid) {
        this.schedulerid = schedulerid;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getWorkFlowInfoJSON() {
        return workFlowInfoJSON;
    }

    public void setWorkFlowInfoJSON(String workFlowInfoJSON) {
        this.workFlowInfoJSON = workFlowInfoJSON;
    }

    public String getExecuteIP() {
        return executeIP;
    }

    public void setExecuteIP(String executeIP) {
        this.executeIP = executeIP;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }
}
