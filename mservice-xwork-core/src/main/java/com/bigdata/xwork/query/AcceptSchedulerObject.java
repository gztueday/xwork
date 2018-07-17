package com.bigdata.xwork.query;

import com.alibaba.fastjson.annotation.JSONField;
import com.bigdata.xwork.dao.entity.Bundle;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zouyi on 2018/1/16.
 */
public class AcceptSchedulerObject {

    private String descript;
    private String schedulerName;
    private String cron;
    private String workFlowName;
    private long submiterid;
    private String requestObj;

    @DateTimeFormat(pattern = DateUtil.FORMAT_TWO)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date startDate;
    @DateTimeFormat(pattern = DateUtil.FORMAT_TWO)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date endDate;
    @DateTimeFormat(pattern = DateUtil.FORMAT_TWO)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date createTime;
    @DateTimeFormat(pattern = DateUtil.FORMAT_TWO)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date updateTime;

    private Map<String, Map<String, String>> arguments;

    private List<Bundle> bundles;

    private boolean delete;

    private long dependVersion;

    public String getRequestObj() {
        return requestObj;
    }

    public void setRequestObj(String requestObj) {
        this.requestObj = requestObj;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getDependVersion() {
        return dependVersion;
    }

    public void setDependVersion(long dependVersion) {
        this.dependVersion = dependVersion;
    }

    public List<Bundle> getBundles() {
        return bundles;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setBundles(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getWorkFlowName() {
        return workFlowName;
    }

    public void setWorkFlowName(String workFlowName) {
        this.workFlowName = workFlowName;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public long getSubmiterid() {
        return submiterid;
    }

    public void setSubmiterid(long submiterid) {
        this.submiterid = submiterid;
    }

    public Map<String, Map<String, String>> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Map<String, String>> arguments) {
        this.arguments = arguments;
    }

}
