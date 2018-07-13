package com.banggood.xwork.action.core;

public enum WorkFlowEvent {
	
	SUBMIT(0),KILL(1),FAIL(2),PAUSE(3),RESUME(4),DONE(5);
	
	private int type;
	
	WorkFlowEvent(int type)
	{
		this.type=type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
