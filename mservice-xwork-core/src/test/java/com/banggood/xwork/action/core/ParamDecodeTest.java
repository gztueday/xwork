package com.banggood.xwork.action.core;

import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.banggood.xwork.action.actions.NormalActionTest;
import org.junit.Test;

import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;

import junit.framework.Assert;

public class ParamDecodeTest {

	@Test
	public void test() throws NoParamException, ParamDataTypeException, ParseActionException, InterruptedException, UnknownHostException {
		
		WorkFlow flow= NormalActionTest.getDemoFlow();
		
		
		CountDownLatch count = new CountDownLatch(1);
		
		flow.getEndWorkAction().setCallBack(count,new WorkRunCallBack(){

			@Override
			public void listen(WorkActionBase action) {
				// TODO Auto-generated method stub
				count.countDown();
				
			}

			@Override
			public void initialize() {
				// TODO Auto-generated method stub
				
			}});
		
		flow.build();
		
		flow.start();
		
		try {
			count.await(200000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WorkRunStatus status = flow.getStatus();

		Assert.assertTrue("flow Topology may be is running or it has been failed",
				status == WorkRunStatus.SUCCESS);
		
	}

}
