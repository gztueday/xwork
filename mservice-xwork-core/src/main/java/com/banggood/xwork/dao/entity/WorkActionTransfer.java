package com.banggood.xwork.dao.entity;

/**
 * Created by zouyi on 2018/1/28.
 */
public class WorkActionTransfer {
    /**
     * 配置里面的workFlowName
     */
    protected String workFlowName;
    /**
     * 配置里面的workActionName
     */
    protected String workActionName;

    public String getWorkFlowName() {
        return workFlowName;
    }

    public void setWorkFlowName(String workFlowName) {
        this.workFlowName = workFlowName;
    }

    public String getWorkActionName() {
        return workActionName;
    }

    public void setWorkActionName(String workActionName) {
        this.workActionName = workActionName;
    }
}
