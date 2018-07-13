package com.banggood.xwork.core.common;


import com.banggood.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.dao.entity.WorkActionTransfer;

public class RemoteWorkAction extends WorkActionTransfer{
    /**
     * 发送请求去的那个workFlow的instanceId
     */
    private String workFlowInstanceid;
    /**
     * 本身的action
     */
    private String remoteActionName;

    private String ip;
    /**
     * 发送请求去哪个action
     */
    private String remoteDependName;
    /**
     * 发送请求的workFlow
     */
    private String remoteWorkFlowName;
    /**
     * 接受请求的workFlow
     */
    private String dependWorkFlowName;

    private WorkRunStatus status;

    private long calculate;

    private String dependSchedulerInstanceid;

    public String getDependSchedulerInstanceid() {
        return dependSchedulerInstanceid;
    }

    public void setDependSchedulerInstanceid(String dependSchedulerInstanceid) {
        this.dependSchedulerInstanceid = dependSchedulerInstanceid;
    }

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

    public String getRemoteWorkFlowName() {
        return remoteWorkFlowName;
    }

    public void setRemoteWorkFlowName(String remoteWorkFlowName) {
        this.remoteWorkFlowName = remoteWorkFlowName;
    }

    public String getRemoteDependName() {
        return remoteDependName;
    }

    public void setRemoteDependName(String remoteDependName) {
        this.remoteDependName = remoteDependName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getWorkFlowInstanceid() {
        return workFlowInstanceid;
    }

    public void setWorkFlowInstanceid(String workFlowInstanceid) {
        this.workFlowInstanceid = workFlowInstanceid;
    }

    public String getRemoteActionName() {
        return remoteActionName;
    }

    @Override
    public String toString() {
        return "RemoteWorkAction{" +
                "workFlowName='" + workFlowName + '\'' +
                ", workActionName='" + workActionName + '\'' +
                ", workFlowInstanceid='" + workFlowInstanceid + '\'' +
                ", remoteActionName='" + remoteActionName + '\'' +
                ", ip='" + ip + '\'' +
                ", remoteDependName='" + remoteDependName + '\'' +
                ", remoteWorkFlowName='" + remoteWorkFlowName + '\'' +
                ", dependWorkFlowName='" + dependWorkFlowName + '\'' +
                ", status=" + status +
                ", calculate=" + calculate +
                ", dependSchedulerInstanceid='" + dependSchedulerInstanceid + '\'' +
                '}';
    }

    public void setRemoteActionName(String remoteActionName) {

        this.remoteActionName = remoteActionName;
    }

    public WorkRunStatus getStatus() {
        return status;
    }

    public void setStatus(WorkRunStatus status) {
        this.status = status;
    }
}
