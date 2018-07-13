package com.banggood.xwork.action.core;

import com.banggood.xwork.action.actions.NormalActionTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.service.WorkDaoService;

public class WorkActionFlowUtilsTest extends BaseTestApplication {

	@Autowired
	WorkDaoService workDaoService;

	@Test
	public void test()
			throws NoParamException, ParamDataTypeException, ParseActionException, CloneNotSupportedException {
		WorkFlow flow = NormalActionTest.getDemoFlow();

		this.workDaoService.addWorkFlow(flow, null);

		try {
			WorkFlow newFlow = this.workDaoService.getWorkFlow(flow.getFlowName());
			newFlow.build();
		} catch (ParseActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
