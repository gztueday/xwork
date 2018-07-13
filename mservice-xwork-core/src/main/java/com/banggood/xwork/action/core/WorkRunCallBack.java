package com.banggood.xwork.action.core;

import java.util.concurrent.CountDownLatch;

import com.banggood.xwork.core.common.AbstractEnviroment;

public abstract class WorkRunCallBack extends AbstractEnviroment{

	private CountDownLatch count;
	
	public abstract void listen(WorkActionBase action);

	public CountDownLatch getCount() {
		return count;
	}

	public void setCount(CountDownLatch count) {
		this.count = count;
	}
}
