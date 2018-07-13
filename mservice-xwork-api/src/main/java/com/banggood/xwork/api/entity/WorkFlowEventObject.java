package com.banggood.xwork.api.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;

@ApiModel(value = "工作流事件", description = "web端发送给Execute的相关事件")
public class WorkFlowEventObject implements Serializable{
    @ApiModelProperty(required = true, dataType = "String", value = "workFlowInstance的id", name = "instanceid", notes = "String类型")
    private String instanceid;

    @ApiModelProperty(required = true, dataType = "int", value = "事件的类型(fail(2),kill(1),resume(4))", name = "eventType", notes = "int类型")
    private int eventType;

    @ApiModelProperty(required = true, dataType = "String", value = "workFlow的名称(name)", name = "flowName", notes = "String类型")
    private String flowName;

    @ApiModelProperty(required = false, dataType = "String", value = "用户", name = "user", notes = "用户权限控制,权限功能暂时未开放")
    private String user;

    @ApiModelProperty(required = true, dataType = "java.util.Map", value = "参数", name = "param", notes = "对workFlowInstance设置的参数")
    private Map<String, String> param;

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, String> getParam() {
        return param;
    }

    public void setParam(Map<String, String> param) {
        this.param = param;
    }

}
