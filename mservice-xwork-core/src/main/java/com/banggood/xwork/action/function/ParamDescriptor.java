package com.banggood.xwork.action.function;

import com.banggood.xwork.action.condition.ConditionDataType;

public class ParamDescriptor {
	
	private String name;
	
	private ConditionDataType dataType;
	
	private int order;
	
	public ParamDescriptor(String name,int order)
	{
		this.name=name;
		this.order=order;
	}
	
	public ParamDescriptor(String name,int order,ConditionDataType dataType)
	{
		this.name=name;
		this.order=order;
		this.dataType=dataType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConditionDataType getDataType() {
		return dataType;
	}

	public void setDataType(ConditionDataType dataType) {
		this.dataType = dataType;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
