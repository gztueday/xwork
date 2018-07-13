package com.banggood.xwork.action.core;

import org.junit.Assert;
import org.junit.Test;

import com.banggood.xwork.action.function.FunctionDescriptor;
import com.banggood.xwork.core.common.FunctionUtils;
import com.banggood.xwork.core.common.FunctionUtils.MatchType;
import com.banggood.xwork.core.exception.MatchException;

public class FunctionUtilsTest {

	@Test
	public void testMatchFunction() throws MatchException {
		
		
		String command="${getHour(-1,'yyyy-MM-dd hh')}";
		if(FunctionUtils.checkParamConfig(command)==MatchType.MATCHFUNCTION)
		{
			FunctionDescriptor function=FunctionUtils.matchFunction(command);
			Assert.assertNotNull("match function error,please check", function.getParamDescriptor()[0]);
		}
		
		
	}
	
	@Test
	public void testInvokeFunction() throws MatchException
	{
		String command="${getHour(-1,'yyyy-MM-dd hh:mm')}";
		if(FunctionUtils.checkParamConfig(command)==MatchType.MATCHFUNCTION)
		{
			FunctionDescriptor function=FunctionUtils.matchFunction(command);
			Assert.assertNotNull("match function error,please check", function.getParamDescriptor()[0]);
			
			FunctionUtils.invokeTimeFunction(function);
		}
	}
	


}
