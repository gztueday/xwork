package com.bigdata.xwork.core.common;


import com.bigdata.xwork.dao.entity.WorkActionTransfer;

public class DependencyWorkAction extends WorkActionTransfer {

    private String remoteDependName;

    private String remoteName;
    /**
     * 是否已经调用开关
     */
    private boolean remoteSwitch = false;
    /**
     * 远程定时任务的id
     */
    private String schedulerid;

    public DependencyWorkAction() {

    }

    public boolean isRemoteSwitch() {
        return remoteSwitch;
    }

    public void setRemoteSwitch(boolean remoteSwitch) {
        this.remoteSwitch = remoteSwitch;
    }

    public String getRemoteDependName() {
        return remoteDependName;
    }

    public void setRemoteDependName(String remoteDependName) {
        this.remoteDependName = remoteDependName;
    }

    public String getSchedulerid() {
        return schedulerid;
    }

    public void setSchedulerid(String schedulerid) {
        this.schedulerid = schedulerid;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    @Override
    public String toString() {
        return "DependencyWorkAction{" +
                "workFlowName='" + workFlowName + '\'' +
                ", remoteDependName='" + remoteDependName + '\'' +
                ", remoteName='" + remoteName + '\'' +
                ", workActionName='" + workActionName + '\'' +
                ", remoteSwitch=" + remoteSwitch +
                ", schedulerid='" + schedulerid + '\'' +
                '}';
    }
}
