package com.bigdata.xwork.dao.entity;

import com.bigdata.xwork.action.core.WorkRunStatus;

/**
 * 都是以发送方为观察方向
 */
public class RemoteWorkFlow {

    private String remoteIp;
    /**
     * 接受请求的workFlow实例的id
     */
    private String remoteWorkFlowInstanceId;
    /**
     * 在接收到remoteWorkFlowInstanceId的Action再设置dependWorkFlowInstanceId
     * 在从内存中找出dependWorkFlowInstanceId中内存的实例
     */
    private String dependWorkFlowInstanceId;

    private String exceptionMessage;
    /**
     * 发送请求的action
     */
    private String remotActionName;
    /**
     * 发送请求的workFlow或则scheduler的名称
     */
    private String remoteWorkFlowName;

    private String dependActionName;

    private String dependWorkFlowName;

    private boolean isSubmit;

    private WorkRunStatus status;
    /**
     * 发送请求方的scheduler名称
     */
    private String remoteSchedulerName;
    /**
     * 接收请求方的scheduler名称
     */
    private String dependSchedulerName;

    public String getRemoteSchedulerName() {
        return remoteSchedulerName;
    }

    public void setRemoteSchedulerName(String remoteSchedulerName) {
        this.remoteSchedulerName = remoteSchedulerName;
    }

    public String getDependSchedulerName() {
        return dependSchedulerName;
    }

    public void setDependSchedulerName(String dependSchedulerName) {
        this.dependSchedulerName = dependSchedulerName;
    }

    public String getDependWorkFlowName() {
        return dependWorkFlowName;
    }

    public void setDependWorkFlowName(String dependWorkFlowName) {
        this.dependWorkFlowName = dependWorkFlowName;
    }

    public WorkRunStatus getStatus() {
        return status;
    }

    public void setStatus(WorkRunStatus status) {
        this.status = status;
    }

    public String getDependActionName() {
        return dependActionName;
    }

    public void setDependActionName(String dependActionName) {
        this.dependActionName = dependActionName;
    }

    public String getRemoteWorkFlowName() {
        return remoteWorkFlowName;
    }

    public void setRemoteWorkFlowName(String remoteWorkFlowName) {
        this.remoteWorkFlowName = remoteWorkFlowName;
    }

    public String getRemotActionName() {
        return remotActionName;
    }

    public void setRemotActionName(String remotActionName) {
        this.remotActionName = remotActionName;
    }

    public boolean isSubmit() {
        return isSubmit;
    }

    public void setSubmit(boolean submit) {
        isSubmit = submit;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemoteWorkFlowInstanceId() {
        return remoteWorkFlowInstanceId;
    }

    public void setRemoteWorkFlowInstanceId(String remoteWorkFlowInstanceId) {
        this.remoteWorkFlowInstanceId = remoteWorkFlowInstanceId;
    }

    public String getDependWorkFlowInstanceId() {
        return dependWorkFlowInstanceId;
    }

    public void setDependWorkFlowInstanceId(String dependWorkFlowInstanceId) {
        this.dependWorkFlowInstanceId = dependWorkFlowInstanceId;
    }

    @Override
    public String toString() {
        return "RemoteWorkFlow{" +
                "remoteIp='" + remoteIp + '\'' +
                ", remoteWorkFlowInstanceId='" + remoteWorkFlowInstanceId + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", remotActionName='" + remotActionName + '\'' +
                ", remoteWorkFlowName='" + remoteWorkFlowName + '\'' +
                ", isSubmit=" + isSubmit +
                '}';
    }
    
}