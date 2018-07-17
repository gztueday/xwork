package com.bigdata.xwork.action.core;

/**
 * Created by zouyi on 2018/3/29.
 */
public class SubWorkFlow {

    private String fatherAction;
    private String subFlowName;
    private String childrenAction;

    public String getFatherAction() {
        return fatherAction;
    }

    public void setFatherAction(String fatherAction) {
        this.fatherAction = fatherAction;
    }

    public String getSubFlowName() {
        return subFlowName;
    }

    public void setSubFlowName(String subFlowName) {
        this.subFlowName = subFlowName;
    }

    public String getChildrenAction() {
        return childrenAction;
    }

    public void setChildrenAction(String childrenAction) {
        this.childrenAction = childrenAction;
    }
}
