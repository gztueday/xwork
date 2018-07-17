package com.bigdata.xwork.action.core;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.bigdata.xwork.action.actions.NormalActionTest;
import com.bigdata.xwork.core.exception.NoParamException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bigdata.xwork.action.impl.DemoAction;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.dao.service.WorkDaoService;

import junit.framework.Assert;

public class    SubWorkActionTest extends BaseTestApplication {

    private static String subWorkName = "sub-flow";

    private static String workName2 = "demo-flow2";

    @Autowired
    private WorkDaoService workDaoService;

    @Test
    public void testSubWorkFlowTopology() throws NoParamException, ParamDataTypeException, ParseActionException, UnknownHostException {


        DemoAction action_1 = NormalActionTest.getDemoAction(100, "action_1", false);

        DemoAction action_2 = NormalActionTest.getDemoAction(100, "action_2", false);

        DemoAction action_3 = NormalActionTest.getDemoAction(100, "action_3", false);

        DemoAction action_4 = NormalActionTest.getDemoAction(100, "action_4", false);

        DemoAction action_5 = NormalActionTest.getDemoAction(100, "action_5", false);

        DemoAction action_6 = NormalActionTest.getDemoAction(100, "action_6", false);

        WorkFlow subWorkFlow = new WorkFlow();
        subWorkFlow.setFlowName(subWorkName);
        subWorkFlow.setWorkDaoService(workDaoService);
        subWorkFlow.putToConfig("runTime", String.valueOf(11111));

        subWorkFlow.putLong("sleep", 200L);
        subWorkFlow.putLong("runTime", System.currentTimeMillis());
        subWorkFlow.initialize();


        subWorkFlow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        subWorkFlow.appendLeaf("action_1", action_2);

        subWorkFlow.appendLeaf("action_1", action_3);

        subWorkFlow.appendLeaf("action_1", action_4);

        subWorkFlow.appendLeaf("action_2", action_5);

        subWorkFlow.appendLeaf("action_3", action_5);

        subWorkFlow.appendLeaf("action_4", action_5);

        subWorkFlow.appendLeaf("action_3", action_6);

        List<WorkActionBase> actions = subWorkFlow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }

        subWorkFlow.getRunParam().putString("sleep", "100");

        subWorkFlow.build();

        CountDownLatch count_2 = new CountDownLatch(1);

        WorkFlow flow_2 = new WorkFlow();
        flow_2.setFlowName(workName2);
        flow_2.setWorkDaoService(workDaoService);
        flow_2.putToConfig("runTime", String.valueOf(11111));

        flow_2.putLong("sleep", 200L);
        flow_2.putLong("runTime", System.currentTimeMillis());
        flow_2.initialize();


        DemoAction baction_1 = NormalActionTest.getDemoAction(100, "baction_1", false);

        DemoAction baction_2 = NormalActionTest.getDemoAction(100, "baction_2", false);

        DemoAction baction_3 = NormalActionTest.getDemoAction(100, "baction_3", false);

        DemoAction baction_4 = NormalActionTest.getDemoAction(100, "baction_4", false);

        flow_2.appendLeaf(WorkActionConstant.STARTACTION, baction_1);

        flow_2.appendLeaf("baction_1", baction_2);

        flow_2.appendLeaf("baction_1", baction_3);


        flow_2.appendLeaf("baction_2", baction_4);

        flow_2.appendLeaf("baction_3", baction_4);

        flow_2.appendSubWorkAction("baction_1", subWorkFlow);

        List<WorkActionBase> actions2 = flow_2.getActions();

        for (WorkActionBase action : actions2) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }


        flow_2.getEndWorkAction().setCallBack(count_2, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {
                // TODO Auto-generated method stub
                count_2.countDown();

            }

            @Override
            public void initialize() {
                // TODO Auto-generated method stub

            }
        });

        flow_2.build();

        try {
            flow_2.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            count_2.await(200000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WorkRunStatus status = flow_2.getStatus();

        Assert.assertTrue("sub work flow Topology may be is running or it has been failed",
                status == WorkRunStatus.SUCCESS);


    }

}
