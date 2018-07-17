package com.bigdata.xwork.action.core;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.bigdata.xwork.action.actions.NormalActionTest;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.core.service.WorkFlowService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bigdata.xwork.action.impl.DemoAction;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.dao.entity.UserInfo;
import com.bigdata.xwork.dao.service.WorkDaoService;

import junit.framework.Assert;

@SuppressWarnings("ALL")
public class WorkFlowPersistentTest extends BaseTestApplication {

    @Autowired
    WorkDaoService workDaoService;

    @Autowired
    WorkFlowService workFlowService;

    private String flowName = "test-work";

    @Test
    public void testSaveWorkFlow()
            throws NoParamException, ParamDataTypeException, ParseActionException, CloneNotSupportedException, InterruptedException, UnknownHostException {
        if (!true) {
            WorkFlow flow_2 = new WorkFlow();
            flow_2.setFlowName("22");

            flow_2.putToConfig("runTime", String.valueOf(System.currentTimeMillis()));

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

            List<WorkActionBase> actions = flow_2.getActions();

            for (WorkActionBase action : actions) {
                if (action instanceof DemoAction) {
                    DemoAction demo = (DemoAction) action;

                    demo.setSleepUseVarible();
                }

            }

            flow_2.build();

            UserInfo user = this.workDaoService.getUserInfoByName("mxq");
            this.workDaoService.addWorkFlow(flow_2, user);

        } else {
            WorkFlow flow;
            try {
                flow = this.workDaoService.getWorkFlow(flowName);
                flow.initialize();
                flow.setWorkDaoService(workDaoService);

              //  flow.getRunParam().putString("sleep", "100");
                flow.getEndWorkAction().setWorkFlowService(workFlowService);
                flow.build();//把关系设置好,即把flow的List<WorkActionBase>设置好(转成实例)
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
                flow.start();
                System.out.println("starting  flow .....");

                count.await(10000, TimeUnit.MILLISECONDS);

                WorkRunStatus status = flow.getStatus();

                Assert.assertTrue("Linear Topology work flow may be is running or it has been failed",
                        status == WorkRunStatus.SUCCESS);

            } catch (ParseActionException e) {

                e.printStackTrace();
            }

        }
    }

}
