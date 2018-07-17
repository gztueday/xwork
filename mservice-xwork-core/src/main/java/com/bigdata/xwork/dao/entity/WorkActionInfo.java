package com.bigdata.xwork.dao.entity;

import com.bigdata.xwork.action.core.WorkActionType;
import com.bigdata.xwork.core.common.WorkConfig;

public class WorkActionInfo extends AbstractInfo {

	public WorkActionInfo() {

	}
	private String cacheRelation ;
	/**
	 * 回调函数类型
	 */
	private String callBackClass;

	/**
	 * 回调参数
	 */
	private WorkConfig callBackParam;

	/**
	 * 动作节点类型
	 */
	private String actionClass;

	/**
	 * 是否共享
	 */
	private boolean share;

	/**
	 * action 名称
	 */
	private String actionName;

	/**
	 * 工作流名称
	 */
	private String flowName;

	/**
	 * action类型
	 */
	private WorkActionType actionType;

	/**
	 * 子工作流名字
	 */
	private String subWorkFlowName;

	public String getCacheRelation() {
		return cacheRelation;
	}

	public void setCacheRelation(String cacheRelation) {
		this.cacheRelation = cacheRelation;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public WorkActionType getActionType() {
		return actionType;
	}

	public void setActionType(WorkActionType actionType) {
		this.actionType = actionType;
	}

	public String getSubWorkFlowName() {
		return subWorkFlowName;
	}

	public void setSubWorkFlowName(String subWorkFlowName) {
		this.subWorkFlowName = subWorkFlowName;
	}

	public String getCallBackClass() {
		return callBackClass;
	}

	public void setCallBackClass(String callBackClass) {
		this.callBackClass = callBackClass;
	}

	public String getActionClass() {
		return actionClass;
	}

	public void setActionClass(String actionClass) {
		this.actionClass = actionClass;
	}

	public WorkConfig getCallBackParam() {
		return callBackParam;
	}

	public void setCallBackParam(WorkConfig callBackParam) {
		this.callBackParam = callBackParam;
	}

}
