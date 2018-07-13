package com.banggood.xwork.action.condition;

public class ConditionCompare {
	
	private ConditionDataType dataType;
	
	private ConditionRelationType relationType;
	
	private String columnKey;
	
	private Object columnValue;
	
	
	public ConditionCompare(String columnKey,Object columnValue)
	{
		this.columnKey=columnKey;
		this.columnValue=columnValue;
	}
	
	public ConditionCompare(ConditionDataType dataType,ConditionRelationType relationType,String columnKey,Object columnValue)
	{
		this.dataType=dataType;
		this.relationType=relationType;
		this.columnKey=columnKey;
		this.columnValue=columnValue;
	}
	
	
	public boolean compareObjct(ConditionCompare condition)
	{
		
		
		switch(this.dataType){
			case BOOLEAN:
				
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  (boolean)condition.columnValue!=(boolean)this.columnValue;
				}else
				{
					return  (boolean)condition.columnValue==(boolean)this.columnValue;
				}
				
				
			case LONG:
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  (long)condition.columnValue!=(long)this.columnValue;
				}else if(this.relationType==ConditionRelationType.EQUAL)
				{
					return  (long)condition.columnValue==(long)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LARGE)
				{
					return  (long)condition.columnValue>(long)this.columnValue;
					
				}else if(this.relationType==ConditionRelationType.LARGETHEN)
				{
					return  (long)condition.columnValue>=(long)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESS)
				{
					return  (long)condition.columnValue<(long)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESSTHEN)
				{
					return  (long)condition.columnValue<=(long)this.columnValue;
				}
				
			case INT:
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  (int)condition.columnValue!=(int)this.columnValue;
				}else if(this.relationType==ConditionRelationType.EQUAL)
				{
					return  (int)condition.columnValue==(int)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LARGE)
				{
					return  (int)condition.columnValue>(int)this.columnValue;
					
				}else if(this.relationType==ConditionRelationType.LARGETHEN)
				{
					return  (int)condition.columnValue>=(int)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESS)
				{
					return  (int)condition.columnValue<(int)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESSTHEN)
				{
					return  (int)condition.columnValue<=(int)this.columnValue;
				}
				
			case FLOAT:
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  (float)condition.columnValue!=(float)this.columnValue;
				}else if(this.relationType==ConditionRelationType.EQUAL)
				{
					return  (float)condition.columnValue==(float)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LARGE)
				{
					return  (float)condition.columnValue>(float)this.columnValue;
					
				}else if(this.relationType==ConditionRelationType.LARGETHEN)
				{
					return  (float)condition.columnValue>=(float)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESS)
				{
					return  (float)condition.columnValue<(float)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESSTHEN)
				{
					return  (float)condition.columnValue<=(float)this.columnValue;
				}
				
			case DOUBLE:
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  (double)condition.columnValue!=(double)this.columnValue;
				}else if(this.relationType==ConditionRelationType.EQUAL)
				{
					return  (double)condition.columnValue==(double)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LARGE)
				{
					return  (double)condition.columnValue>(double)this.columnValue;
					
				}else if(this.relationType==ConditionRelationType.LARGETHEN)
				{
					return  (double)condition.columnValue>=(double)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESS)
				{
					return  (double)condition.columnValue<(double)this.columnValue;
				}else if(this.relationType==ConditionRelationType.LESSTHEN)
				{
					return  (double)condition.columnValue<=(double)this.columnValue;
				}
				
				
			case STRING:
				if(this.relationType==ConditionRelationType.NOEQUAL)
				{
					return  !condition.columnValue.equals(this.columnValue);
				}else if(this.relationType==ConditionRelationType.EQUAL)
				{
					return  condition.columnValue.equals(this.columnValue);
				}
				
			default:break;
			
		
		}
			
		
		
		return false;
	}


	public ConditionDataType getDataType() {
		return dataType;
	}


	public void setDataType(ConditionDataType dataType) {
		this.dataType = dataType;
	}


	public ConditionRelationType getRelationType() {
		return relationType;
	}


	public void setRelationType(ConditionRelationType relationType) {
		this.relationType = relationType;
	}


	public String getColumnKey() {
		return columnKey;
	}


	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}


	public Object getColumnValue() {
		return columnValue;
	}


	public void setColumnValue(Object columnValue) {
		this.columnValue = columnValue;
	}

}
