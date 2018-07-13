package com.banggood.xwork.dao.entity;

import java.util.List;

/**
 * 都是以发送方为观察方向
 */
public class RemoteCoordinators {
    /**
     * 发送方的workFlow名称
     */
    private String remoteWorkFlowName;
    private String remoteAcionName;
    private String remoteWorkFlowIp;
    /**
     * 以发送为参考方向:
     * 发送方的schedulerInstanceId
     */
    private String remoteSchedulerInstanceId;

    private String dependWorkFlowName;
    private List<String> dependActionName;
    private String dependWorkFlowIp;
    /**
     * 以发送为参考方向:
     * 接收方的schedulerInstanceId
     */
    private String dependSchedulerInstanceId;

    private String relationId;

    public String getRemoteSchedulerInstanceId() {
        return remoteSchedulerInstanceId;
    }

    public void setRemoteSchedulerInstanceId(String remoteSchedulerInstanceId) {
        this.remoteSchedulerInstanceId = remoteSchedulerInstanceId;
    }

    public String getDependSchedulerInstanceId() {
        return dependSchedulerInstanceId;
    }

    public void setDependSchedulerInstanceId(String dependSchedulerInstanceId) {
        this.dependSchedulerInstanceId = dependSchedulerInstanceId;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getRemoteWorkFlowName() {
        return remoteWorkFlowName;
    }

    public void setRemoteWorkFlowName(String remoteWorkFlowName) {
        this.remoteWorkFlowName = remoteWorkFlowName;
    }

    public String getRemoteAcionName() {
        return remoteAcionName;
    }

    public void setRemoteAcionName(String remoteAcionName) {
        this.remoteAcionName = remoteAcionName;
    }

    public String getRemoteWorkFlowIp() {
        return remoteWorkFlowIp;
    }

    public void setRemoteWorkFlowIp(String remoteWorkFlowIp) {
        this.remoteWorkFlowIp = remoteWorkFlowIp;
    }

    public String getDependWorkFlowName() {
        return dependWorkFlowName;
    }

    public void setDependWorkFlowName(String dependWorkFlowName) {
        this.dependWorkFlowName = dependWorkFlowName;
    }

    public List<String> getDependActionName() {
        return dependActionName;
    }

    public void setDependActionName(List<String> dependActionName) {
        this.dependActionName = dependActionName;
    }

    public String getDependWorkFlowIp() {
        return dependWorkFlowIp;
    }

    public void setDependWorkFlowIp(String dependWorkFlowIp) {
        this.dependWorkFlowIp = dependWorkFlowIp;
    }
}
