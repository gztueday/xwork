package com.bigdata.xwork.action.core;

import com.bigdata.xwork.action.actions.NormalActionTest;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.dao.service.WorkDaoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
