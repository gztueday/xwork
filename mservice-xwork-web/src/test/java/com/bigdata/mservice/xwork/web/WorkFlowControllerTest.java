package com.bigdata.mservice.xwork.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.dao.entity.WorkFlowConfig;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionConstant;
import com.banggood.xwork.action.core.WorkFlow;
import com.banggood.xwork.action.core.WorkFlowEvent;
import com.banggood.xwork.action.impl.DemoAction;
import com.banggood.xwork.api.entity.WorkFlowEventObject;
import com.banggood.xwork.core.common.FlowActionUtils;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.WorkActionService.WorkActionDescription;
import com.banggood.xwork.dao.service.WorkDaoService;


@SuppressWarnings("ALL")
public class WorkFlowControllerTest extends BaseApplication {

    @Autowired
    WorkDaoService workDaoService;


    @Autowired
    FlowActionUtils flowActionUtils;

    @Test
    public void test() throws NoParamException, ParamDataTypeException, ParseActionException, IOException {

        DemoAction action_1 = new DemoAction();
        action_1.putToConfig("sleep", "300");
        action_1.setActionName("action_1");

        DemoAction action_2 = new DemoAction();
        action_2.putToConfig("sleep", "300");
        action_2.setActionName("action_2");

        DemoAction action_3 = new DemoAction();
        action_3.putToConfig("sleep", "300");
        action_3.setActionName("action_3");

        DemoAction action_4 = new DemoAction();
        action_4.putToConfig("sleep", "300");
        action_4.setActionName("action_4");

        WorkFlow flow = new WorkFlow();
        flow.setFlowName("testbuild2");

        flow.setWorkDaoService(workDaoService);
        flow.putToConfig("runTime", String.valueOf(11111));

        flow.putLong("sleep", 200L);
        flow.putLong("runTime", System.currentTimeMillis());
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_1", action_3);

        flow.appendLeaf("action_2", action_4);

        flow.appendLeaf("action_3", action_4);

        flow.build();

        WorkFlowConfig config = new WorkFlowConfig();

        config.setFlowName(flow.getFlowName());

        config.setRelations(flow.getRelations());

        config.setFlowConfig(flow.getConfig());

        List<WorkActionBase> actions = flow.getActions();

        List<WorkActionDescription> descriptors = new ArrayList<>();
        for (WorkActionBase action : actions) {
            WorkActionDescription descriptor = flowActionUtils.getWorkActionDescription(action);
            descriptors.add(descriptor);
        }


        config.setDescriptors(descriptors);


        RequestParam param = new RequestParam();
        param.setPname("workFlowConfig");
        param.setPvalue(JSONObject.toJSONString(config));

        System.out.println(JSONObject.toJSONString(config));

        try {
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/xwork/flow/create").param(param.getPname(), param.getPvalue())).andReturn();
            int status = mvcResult.getResponse().getStatus();
            String content = mvcResult.getResponse().getContentAsString();
            System.out.println("--------------content------------->>");
            System.out.println(content);
            System.out.println("<<--------------content-------------");
            Assert.assertTrue("错误，正确的返回值为200", status == 200);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Test
    public void testSubmitWorkFLow() {

        WorkFlowEventObject event = new WorkFlowEventObject();
        event.setFlowName("test-work");
        event.setEventType(WorkFlowEvent.SUBMIT.getType());
        Map<String, String> param = new HashMap<String, String>();
        param.put("sleep", "100");
        param.put("runTime", String.valueOf(System.currentTimeMillis()));
        event.setParam(param);


        try {
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/xwork/flow/submit").param("event", JSONObject.toJSONString(event))).andReturn();
            int status = mvcResult.getResponse().getStatus();
            String content = mvcResult.getResponse().getContentAsString();
            System.out.println("--------------content------------->>");
            System.out.println(content);
            System.out.println("<<--------------content-------------");
            Assert.assertTrue("错误，正确的返回值为200", status == 200);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
