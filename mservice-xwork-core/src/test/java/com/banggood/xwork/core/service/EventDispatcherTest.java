package com.banggood.xwork.core.service;

import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.BaseTestApplication;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionConstant;
import com.banggood.xwork.action.core.WorkFlow;
import com.banggood.xwork.action.core.WorkFlowEvent;
import com.banggood.xwork.action.core.WorkRunCallBack;
import com.banggood.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.action.impl.DemoAction;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.WorkFlowEventObject;
import com.banggood.xwork.core.common.FlowActionUtils;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.WorkActionService.WorkActionDescription;
import com.banggood.xwork.dao.service.WorkDaoService;

@SuppressWarnings("ALL")
public class EventDispatcherTest extends BaseTestApplication {

    @Autowired
    ExecutorDispatcher eventDispatcher;

    @Autowired
    WorkActionService workActionService;

    @Autowired
    FlowActionUtils workActionFlowUtils;

    @Autowired
    WorkFlowService workFlowService;

    @Autowired
    WorkDaoService workDaoService;

    @Test
    public void testBuidWorkFLow() throws InstantiationException, IllegalAccessException, NoParamException,
            ParamDataTypeException, ParseActionException, UnknownHostException, ClassNotFoundException {
        WorkActionDescription description = this.workActionService.getWorkActionDescriptionByClazz(DemoAction.class);
        Assert.assertNotNull(description);

        WorkActionBase action_1 = workActionFlowUtils.copyAction(description.getClazz());
        action_1.setActionName("action_1");

        WorkActionBase action_2 = workActionFlowUtils.copyAction(description.getClazz());
        action_2.setActionName("action_2");

        WorkActionBase action_3 = workActionFlowUtils.copyAction(description.getClazz());
        action_3.setActionName("action_3");

        WorkFlow flow = new WorkFlow();
        flow.setFlowName("auto-build");
        flow.initialize();

        flow.appendLeaf(WorkActionConstant.STARTACTION, action_1);

        flow.appendLeaf("action_1", action_2);

        flow.appendLeaf("action_1", action_3);

        List<WorkActionBase> actions = flow.getActions();

        for (WorkActionBase action : actions) {
            if (action instanceof DemoAction) {
                DemoAction demo = (DemoAction) action;

                demo.setSleepUseVarible();
            }

        }

        flow.getRunParam().putString("sleep", "100");

        flow.build();

        CountDownLatch count = new CountDownLatch(1);

        flow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

            @Override
            public void listen(WorkActionBase action) {

                count.countDown();

            }

            @Override
            public void initialize() {


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

    @Test
    public void testDispatcher() throws NoParamException, ParamDataTypeException, InstantiationException,
            IllegalAccessException, InterruptedException, UnknownHostException {

//        WorkFlowEventObject event = new WorkFlowEventObject();
//        event.setFlowName("test-work");
//        event.setEventType(WorkFlowEvent.SUBMIT.getType());
//        Map<String, String> param = new HashMap<>();
//        param.put("sleep", "100");
//        param.put("runTime", String.valueOf(System.currentTimeMillis()));
//        event.setParam(param);
//
//        EventResponse resp = this.eventDispatcher.dispatcher(event);
//        if (resp.getStatus() == 1) {
//            String instanceid = JSONObject.parseObject(resp.getJson()).getString("instanceid");
//            WorkFlow instance = this.workFlowService.getWorkFlowInstance(instanceid);
//            CountDownLatch count = new CountDownLatch(1);
//
//            instance.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {
//
//                @Override
//                public void listen(WorkActionBase action) {
//
//                    count.countDown();
//
//                }
//
//                @Override
//                public void initialize() {
//
//                }
//            });
//
//
//            count.await(2000000, TimeUnit.MILLISECONDS);
//
//            WorkRunStatus status = instance.getStatus();
//
//            Assert.assertTrue("Complication Topology work flow may be is running or it has been failed",
//                    status == WorkRunStatus.SUCCESS);
//        } else {
//            fail("submit work flow error,please check");
//        }

    }

    @Test
    public void testKillWorkFlow() throws InterruptedException, UnknownHostException {
//        WorkFlowEventObject event = new WorkFlowEventObject();
//        event.setFlowName("test-work");
//        event.setEventType(WorkFlowEvent.SUBMIT.getType());
//        Map<String, String> param = new HashMap<>();
//        param.put("sleep", "200");
//        param.put("runTime", String.valueOf(System.currentTimeMillis()));
//        event.setParam(param);
//
//        EventResponse resp = this.eventDispatcher.dispatcher(event);
//        if (resp.getStatus() == 1) {
//            String instanceid = JSONObject.parseObject(resp.getJson()).getString("instanceid");
//            WorkFlow instance = this.workFlowService.getWorkFlowInstance(instanceid);
//            CountDownLatch count = new CountDownLatch(1);
//
//            instance.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {
//
//                @Override
//                public void listen(WorkActionBase action) {
//
//                    count.countDown();
//
//                }
//
//                @Override
//                public void initialize() {
//
//
//                }
//            });
//
//            Thread.sleep(3000);
//            System.out.println("try to kill work flow instance:" + instanceid);
//            WorkFlowEventObject killEvent = new WorkFlowEventObject();
//            killEvent.setInstanceid(instanceid);
//            killEvent.setEventType(WorkFlowEvent.KILL.getType());
//
//            this.eventDispatcher.dispatcher(killEvent);
//
//            WorkRunStatus status = instance.getStatus();
//
//            count.await(200000, TimeUnit.MILLISECONDS);
//
//            Assert.assertTrue("Complication Topology work flow may be is running or it has been failed",
//                    status == WorkRunStatus.KILLED);
//
//        } else {
//            fail("submit work flow error,please check");
//
//        }
    }


    @Test
    public void testResumeWorkFlow() throws InterruptedException, UnknownHostException {
//        WorkFlowEventObject event = new WorkFlowEventObject();
//        event.setFlowName("test-work");
//        event.setEventType(WorkFlowEvent.SUBMIT.getType());
//        Map<String, String> param = new HashMap<String, String>();
//        param.put("sleep", "200");
//        param.put("runTime", String.valueOf(System.currentTimeMillis()));
//        event.setParam(param);
//
//        EventResponse resp = this.eventDispatcher.dispatcher(event);
//        if (resp.getStatus() == 1) {
//            String instanceid = JSONObject.parseObject(resp.getJson()).getString("instanceid");
//            WorkFlow instance = this.workFlowService.getWorkFlowInstance(instanceid);
//            CountDownLatch count = new CountDownLatch(1);
//
//            instance.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {
//
//                @Override
//                public void listen(WorkActionBase action) {
//                    // TODO Auto-generated method stub
//                    count.countDown();
//
//                }
//
//                @Override
//                public void initialize() {
//                    // TODO Auto-generated method stub
//
//                }
//            });
//
//            Thread.sleep(3000);
//            System.out.println("try to kill work flow instance:" + instanceid);
//            WorkFlowEventObject killEvent = new WorkFlowEventObject();
//            killEvent.setInstanceid(instanceid);
//            killEvent.setEventType(WorkFlowEvent.KILL.getType());
//
//            this.eventDispatcher.dispatcher(killEvent);
//
//            WorkRunStatus status = instance.getStatus();
//
//            count.await(200000, TimeUnit.MILLISECONDS);
//
//            Assert.assertTrue("Complication Topology work flow may be is running or it has been failed",
//                    status == WorkRunStatus.KILLED);
//
//            Thread.sleep(3000);
//
//            System.out.println("try to resume work flow:" + instance.getInstanceid());
//
//            WorkFlowEventObject resumeEvent = new WorkFlowEventObject();
//            resumeEvent.setInstanceid(instanceid);
//            resumeEvent.setEventType(WorkFlowEvent.RESUME.getType());
//
//            CountDownLatch newCount = new CountDownLatch(1);
//
//            instance.getEndWorkAction().setCallBack(newCount, new WorkRunCallBack() {
//
//                @Override
//                public void listen(WorkActionBase action) {
//                    // TODO Auto-generated method stub
//                    newCount.countDown();
//
//                }
//
//                @Override
//                public void initialize() {
//                    // TODO Auto-generated method stub
//
//                }
//            });
//
//            this.eventDispatcher.dispatcher(resumeEvent);
//
//            newCount.await(200000, TimeUnit.MILLISECONDS);
//
//            status = instance.getStatus();
//
//            Assert.assertTrue("Complication Topology work flow may be is running or it has been failed",
//                    status == WorkRunStatus.SUCCESS);
//
//        } else {
//            fail("submit work flow error,please check");
//        }
    }

}
