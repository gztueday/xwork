package com.bigdata.xwork.dao.entity;

/**
 * 运行实例实
 */
public class WorkActionInstance extends AbtractInstance{

	private String flowInstanceid;

	/**
	 * 工作流名称
	 */
	private String flowName;

	/**
	 * 节点名称
	 */
	private String actionName;

	/**
	 * 调度ID
	 */
	private String schedulerid;

	/**
	 * 输出参数
	 */
	private String outPutJSON;
	
	/**
	 * 节点类全名
	 * */
	private String clazz;
	
	
	private boolean conditionPass;

	public String getSchedulerid() {
		return schedulerid;
	}

	public void setSchedulerid(String schedulerid) {
		this.schedulerid = schedulerid;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}


	public String getOutPutJSON() {
		return outPutJSON;
	}

	public void setOutPutJSON(String outPutJSON) {
		this.outPutJSON = outPutJSON;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public boolean isConditionPass() {
		return conditionPass;
	}

	public void setConditionPass(boolean conditionPass) {
		this.conditionPass = conditionPass;
	}

	public String getFlowInstanceid() {
		return flowInstanceid;
	}

	public void setFlowInstanceid(String flowInstanceid) {
		this.flowInstanceid = flowInstanceid;
	}



}
