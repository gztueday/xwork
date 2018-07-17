package com.bigdata.xwork.action.actions;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.banggood.xwork.action.core.*;
import com.bigdata.xwork.action.core.*;
import com.bigdata.xwork.core.exception.NoParamException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bigdata.xwork.action.impl.DemoAction;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.dao.service.WorkDaoService;

import junit.framework.Assert;

public class NormalActionTest extends BaseTestApplication {

    private static String workName = "demo-flow";

    private static String workName2 = "demo-flow2";

    @Autowired
    WorkDaoService workDaoService;

    private static String uuid = java.util.UUID.randomUUID().toString();

    @Test
    public void testLinearTopology()
            throws InterruptedException, NoParamException, ParamDataTypeException, ParseActionException, UnknownHostException {

        CountDownLatch count = new CountDownLatch(1);

        DemoAction action_1 = getDemoAction(100, "action_1", false);

        DemoAction action_2 = getDemoAction(100, "action_2", false);

        DemoAction action_3 = getDemoAction(100, "action_3", false);

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(workName);
        flow.setWorkDaoService(workDaoService);
        flow.putToConfig("runTime", String.valueOf(11111));

        flow.putLong("sleep", 200L);
        flow.putLong("runTime", System.currentTimeMillis());
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_2", action_3);

        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }


        flow.build();

        flow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {
                // TODO Auto-generated method stub
                count.countDown();

            }

            @Override
            public void initialize() {
                // TODO Auto-generated method stub

            }
        });

        flow.start();

        count.await(200000, TimeUnit.MILLISECONDS);

        WorkRunStatus status = flow.getStatus();

        Assert.assertTrue("Linear Topology work flow may be is running or it has been failed",
                status == WorkRunStatus.SUCCESS);

    }

    public void testLinearTopologyFail()
            throws InterruptedException, NoParamException, ParamDataTypeException, ParseActionException, UnknownHostException {

        CountDownLatch count = new CountDownLatch(1);

        DemoAction action_1 = getDemoAction(100, "action_1", false);

        DemoAction action_2 = getDemoAction(100, "action_2", true);

        DemoAction action_3 = getDemoAction(100, "action_3", false);

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(workName2);

        flow.setWorkDaoService(workDaoService);
        flow.putToConfig("runTime", String.valueOf(11111));

        flow.putLong("sleep", 200L);
        flow.putLong("runTime", System.currentTimeMillis());
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_2", action_3);

        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }


        flow.build();

        flow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {
                // TODO Auto-generated method stub
                count.countDown();

            }

            @Override
            public void initialize() {
                // TODO Auto-generated method stub

            }
        });

        flow.start();


        count.await(200000, TimeUnit.MILLISECONDS);

        WorkRunStatus status = flow.getStatus();

        Assert.assertTrue("Linear Topology work flow may be is running or it has been failed",
                status == WorkRunStatus.SUCCESS);

    }

    @Test
    public void testTwoWorkFlowTopoloy() throws NoParamException, ParamDataTypeException, ParseActionException, UnknownHostException {

        CountDownLatch count = new CountDownLatch(1);

        DemoAction action_1 = getDemoAction(100, "action_1", false);

        DemoAction action_2 = getDemoAction(100, "action_2", false);

        DemoAction action_3 = getDemoAction(100, "action_3", false);

        DemoAction action_4 = getDemoAction(100, "action_4", false);

        DemoAction action_5 = getDemoAction(100, "action_5", false);

        DemoAction action_6 = getDemoAction(100, "action_6", false);

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(workName);
        flow.setWorkDaoService(workDaoService);
        flow.putToConfig("runTime", String.valueOf(11111));

        flow.putLong("sleep", 200L);
        flow.putLong("runTime", System.currentTimeMillis());
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_1", action_3);

        flow.appendLeaf("action_1", action_4);

        flow.appendLeaf("action_2", action_5);

        flow.appendLeaf("action_3", action_5);

        flow.appendLeaf("action_4", action_5);

        flow.appendLeaf("action_3", action_6);


        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }


        flow.build();

        flow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {
                // TODO Auto-generated method stub
                count.countDown();

            }

            @Override
            public void initialize() {
                // TODO Auto-generated method stub

            }
        });

        CountDownLatch count_2 = new CountDownLatch(1);

        WorkFlow flow_2 = new WorkFlow();
        flow_2.setFlowName(workName2);
        flow_2.setWorkDaoService(workDaoService);
        flow_2.putToConfig("runTime", String.valueOf(11111));

        flow_2.putLong("sleep", 200L);
        flow_2.putLong("runTime", System.currentTimeMillis());
        flow_2.initialize();

        DemoAction baction_1 = getDemoAction(100, "baction_1", false);

        DemoAction baction_2 = getDemoAction(100, "baction_2", false);

        DemoAction baction_3 = getDemoAction(100, "baction_3", false);

        DemoAction baction_4 = getDemoAction(100, "baction_4", false);

        flow_2.appendLeaf(WorkActionConstant.STARTACTION, baction_1);

        flow_2.appendLeaf("baction_1", baction_2);

        flow_2.appendLeaf("baction_1", baction_3);

        flow_2.appendLeaf("baction_2", baction_4);

        flow_2.appendLeaf("baction_3", baction_4);

        flow_2.appendLeaf("baction_3", action_2);


        List<WorkActionBase> actions2 = flow_2.getActions();

        for (WorkActionBase action : actions2) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }


        flow_2.build();

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

        // start flow
        try {
            flow.start();
            flow_2.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            count.await(200000, TimeUnit.MILLISECONDS);
            count_2.await(200000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WorkRunStatus status = flow.getStatus();

        Assert.assertTrue("work-flow may be is running or it has been failed", status == WorkRunStatus.SUCCESS);

        WorkRunStatus status2 = flow_2.getStatus();

        Assert.assertTrue("work-flow2 may be is running or it has been failed", status2 == WorkRunStatus.SUCCESS);

    }

    @Test
    public void testComplicationTopoloy() throws NoParamException, ParamDataTypeException, ParseActionException, UnknownHostException {

        CountDownLatch count = new CountDownLatch(1);

        DemoAction action_1 = getDemoAction(100, "action_1", false);

        DemoAction action_2 = getDemoAction(100, "action_2", false);

        DemoAction action_3 = getDemoAction(100, "action_3", false);

        DemoAction action_4 = getDemoAction(100, "action_4", false);

        DemoAction action_5 = getDemoAction(100, "action_5", false);

        DemoAction action_6 = getDemoAction(100, "action_6", false);

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(workName);
        flow.setWorkDaoService(workDaoService);
        flow.putToConfig("runTime", String.valueOf(11111));

        flow.putLong("sleep", 200L);
        flow.putLong("runTime", System.currentTimeMillis());
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_1", action_3);

        flow.appendLeaf("action_1", action_4);

        flow.appendLeaf("action_2", action_5);

        flow.appendLeaf("action_3", action_5);

        flow.appendLeaf("action_4", action_5);

        flow.appendLeaf("action_3", action_6);

        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }

        flow.getRunParam().putString("sleep", "100");
        flow.setWorkDaoService(workDaoService);
        flow.build();

        flow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {
                // TODO Auto-generated method stub
                count.countDown();

            }

            @Override
            public void initialize() {
                // TODO Auto-generated method stub

            }
        });

        try {
            flow.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            count.await(200000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WorkRunStatus status = flow.getStatus();

        Assert.assertTrue("Complication Topology work flow may be is running or it has been failed",
                status == WorkRunStatus.SUCCESS);

    }

    public static DemoAction getDemoAction(long sleep, String actionName, boolean throwException) {

        DemoAction action = new DemoAction();
        action.setActionName(actionName);

        return action;
    }

    public static WorkFlow getDemoFlow() throws NoParamException, ParamDataTypeException, ParseActionException {
        CountDownLatch count = new CountDownLatch(6);

        DemoAction action_1 = getDemoAction(100, "action_1", false);

        DemoAction action_2 = getDemoAction(100, "action_2", false);

        DemoAction action_3 = getDemoAction(100, "action_3", false);

        DemoAction action_4 = getDemoAction(100, "action_4", false);

        DemoAction action_5 = getDemoAction(100, "action_5", false);

        DemoAction action_6 = getDemoAction(100, "action_6", false);

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(workName);
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_1", action_3);

        flow.appendLeaf("action_1", action_4);

        flow.appendLeaf("action_2", action_5);

        flow.appendLeaf("action_3", action_5);

        flow.appendLeaf("action_4", action_5);

        flow.appendLeaf("action_3", action_6);

        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }

        flow.putString("sleep", "100");

        return flow;
    }

}
