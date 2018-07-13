package com.banggood.xwork.scheduler.core;

import com.banggood.xwork.action.core.WorkActionParam;
import com.banggood.xwork.action.core.WorkFlow;
import com.banggood.xwork.core.common.AbstractEnviroment;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 调度器，每一个work action flow都配一个调度实例，work action配置的东西 work action
 * flow参数都通过schedule传入
 */
public class WorkScheduler extends AbstractEnviroment {

    /**
     * 实例ID
     */
    private String instanceid;

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
    private String SchedulerName;

    private boolean schedulerStatus = false;
    /**
     * 远程调度workFlow的名称集合
     */
    private List<String> workFlowNames = new ArrayList<>();

    private boolean remoteScheduler = false;
    /**
     *
     * */
    private long startTime;

    /**
     *
     * */
    private long endTime;

    /**
     *
     * */
    private long runTime;

    /**
     * 配置的用户
     */
    private long configerid;

    /**
     * 更新的用户
     */
    private long updaterid;

    /**
     * 提交用户
     */
    private long submiter;

    private WorkFlow workFlow;

    //---------------------------------------------------------------------------------------------

    private String workFlowInstanceId;

    private String ip;

    private long versions;

    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getWorkFlowInstanceId() {
        return workFlowInstanceId;
    }

    public void setWorkFlowInstanceId(String workFlowInstanceId) {
        this.workFlowInstanceId = workFlowInstanceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public WorkScheduler() {
        this.registerParam("runTime", new WorkActionParam(true, WorkActionParam.ParamDataType.STRING));
    }

    public WorkFlow getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(WorkFlow workFlow) {
        this.workFlow = workFlow;
    }

    public String getCronStr() {
        return cronStr;
    }

    public void setCronStr(String cronStr) {
        this.cronStr = cronStr;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getSchedulerName() {
        return SchedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        SchedulerName = schedulerName;
    }

    public boolean isRemoteScheduler() {
        return remoteScheduler;
    }

    public void setRemoteScheduler(boolean remoteScheduler) {
        this.remoteScheduler = remoteScheduler;
    }

    @Override
    public void initialize() {

    }

    public List<String> getWorkFlowNames() {
        return workFlowNames;
    }

    public void setWorkFlowNames(List<String> workFlowNames) {
        this.workFlowNames = workFlowNames;
    }

    public boolean getSchedulerStatus() {
        return schedulerStatus;
    }

    public void setSchedulerStatus(boolean schedulerStatus) {
        this.schedulerStatus = schedulerStatus;
    }

    public long getSubmiter() {
        return submiter;
    }

    public void setSubmiter(long submiter) {
        this.submiter = submiter;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public long getConfigerid() {
        return configerid;
    }

    public void setConfigerid(long configerid) {
        this.configerid = configerid;
    }

    public long getUpdaterid() {
        return updaterid;
    }

    public void setUpdaterid(long updaterid) {
        this.updaterid = updaterid;
    }

    public long getVersions() {
        return versions;
    }

    public void setVersions(long versions) {
        this.versions = versions;
    }

    public void build() {
        this.setInstanceid(format());
        this.getWorkFlow().setSchedulerid(this.getInstanceid());
    }

    public String format() {
        StringBuffer sb = new StringBuffer();
        sb.append("wf_");
        sb.append(this.SchedulerName);
        sb.append("_");
        String uuid = UUID.randomUUID().toString();
        sb.append(uuid);
        sb.append("_");
        sb.append(this.startTime);

        return sb.toString();
    }

    @Override
    public String toString() {
        return "WorkScheduler{" +
                ", instanceid='" + instanceid + '\'' +
                ", cronStr='" + cronStr + '\'' +
                ", flowName='" + flowName + '\'' +
                ", SchedulerName='" + SchedulerName + '\'' +
                ", schedulerStatus=" + schedulerStatus +
                ", workFlowNames=" + workFlowNames +
                ", remoteScheduler=" + remoteScheduler +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", runTime=" + runTime +
                ", configerid=" + configerid +
                ", updaterid=" + updaterid +
                ", submiter=" + submiter +
                ", workFlow=" + workFlow +
                ", ip='" + ip + '\'' +
                ", versions=" + versions +
                '}';
    }

    public boolean isSchedulerStatus() {
        return schedulerStatus;
    }
}
