package com.bigdata.xwork.action.function;

import com.bigdata.xwork.action.condition.ConditionDataType;

public class FunctionDescriptor {
	
	private String name;
	
	private String desc;
	
	private String usage;
	
	private ConditionDataType returnDataType;
	
	private ParamDescriptor[] paramDescriptor;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public ParamDescriptor[] getParamDescriptor() {
		return paramDescriptor;
	}

	public void setParamDescriptor(ParamDescriptor[] paramDescriptor) {
		this.paramDescriptor = paramDescriptor;
	}

	public ConditionDataType getReturnDataType() {
		return returnDataType;
	}

	public void setReturnDataType(ConditionDataType returnDataType) {
		this.returnDataType = returnDataType;
	}

}
