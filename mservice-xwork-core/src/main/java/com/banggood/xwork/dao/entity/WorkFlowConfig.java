package com.banggood.xwork.dao.entity;

import java.util.List;
import java.util.Map;

import com.banggood.xwork.action.core.SubWorkFlow;
import com.banggood.xwork.action.core.WorkActionRelation;
import com.banggood.xwork.core.common.WorkConfig;
import com.banggood.xwork.core.service.WorkActionService.WorkActionDescription;

public class WorkFlowConfig {

    private List<WorkActionDescription> descriptors;

    private Map<String, WorkActionRelation> relations;

    private List<String> emails;

    private String flowName;

    private WorkConfig flowConfig;

    private String uuid;
    /**
     * 前端回显需要保存的数据
     */
    private String requestsObj;
    /**
     * 前端联系回显需要的数据
     */
    private String relationsObj;

    private Map<String, Map<String, String>> params;
    private String paramsJson;

    private String description;

    private List<SubWorkFlow> subWorkFlows;

    private String subWorkFlowJson;

    public String getSubWorkFlowJson() {
        return subWorkFlowJson;
    }

    public void setSubWorkFlowJson(String subWorkFlowJson) {
        this.subWorkFlowJson = subWorkFlowJson;
    }

    public List<SubWorkFlow> getSubWorkFlows() {
        return subWorkFlows;
    }

    public void setSubWorkFlows(List<SubWorkFlow> subWorkFlows) {
        this.subWorkFlows = subWorkFlows;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Map<String, String>> getParams() {
        return params;
    }

    public void setParams(Map<String, Map<String, String>> params) {
        this.params = params;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getRelationsObj() {
        return relationsObj;
    }

    public void setRelationsObj(String relationsObj) {
        this.relationsObj = relationsObj;
    }

    private boolean open = false;

    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRequestObj() {
        return requestsObj;
    }

    public void setRequestObj(String requestObj) {
        this.requestsObj = requestObj;
    }

    public Map<String, WorkActionRelation> getRelations() {
        return relations;
    }


    public void setRelations(Map<String, WorkActionRelation> relations) {
        this.relations = relations;
    }


    public List<WorkActionDescription> getDescriptors() {
        return descriptors;
    }


    public void setDescriptors(List<WorkActionDescription> descriptors) {
        this.descriptors = descriptors;
    }


    public String getFlowName() {
        return flowName;
    }


    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }


    public WorkConfig getFlowConfig() {
        return flowConfig;
    }


    public void setFlowConfig(WorkConfig flowConfig) {
        this.flowConfig = flowConfig;
    }


    public List<String> getEmails() {
        return emails;
    }


    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
