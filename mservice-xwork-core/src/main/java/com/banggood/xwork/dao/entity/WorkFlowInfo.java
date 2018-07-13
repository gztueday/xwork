package com.banggood.xwork.dao.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.sql.Timestamp;

public class WorkFlowInfo extends AbstractInfo {

    private String emailJson;

    private String flowName;

    private String relationsJson;

    private String actions;

    private boolean delete = false;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastUpdateTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());

    private String dependName;

    private String emails;

    /**
     * 前端数据回显需要
     */
    private String requestObj;

    /**
     * 编辑更新workFlow配置时需要用的uuid
     */
    private String uuid;
    /**
     * work_flow表依赖外键列
     */
    private String flowRelation;

    private String showActions;

    private String paramJson;

    private String description;

    private String subWorkFlowJson;

    public String getSubWorkFlowJson() {
        return subWorkFlowJson;
    }

    public void setSubWorkFlowJson(String subWorkFlowJson) {
        this.subWorkFlowJson = subWorkFlowJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParamJson() {
        return paramJson;
    }

    public void setParamJson(String paramJson) {
        this.paramJson = paramJson;
    }

    public String getShowActions() {
        return showActions;
    }

    public void setShowActions(String showActions) {
        this.showActions = showActions;
    }

    public String getFlowRelation() {
        return flowRelation;
    }

    public void setFlowRelation(String flowRelation) {
        this.flowRelation = flowRelation;
    }

    public String getRequestObj() {
        return requestObj;
    }

    public void setRequestObj(String requestObj) {
        this.requestObj = requestObj;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getDependName() {
        return dependName;
    }

    public void setDependName(String dependName) {
        this.dependName = dependName;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getRelationsJson() {
        return relationsJson;
    }

    public void setRelationsJson(String relationsJson) {
        this.relationsJson = relationsJson;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getEmailJson() {
        return emailJson;
    }

    public void setEmailJson(String emailJson) {
        this.emailJson = emailJson;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
