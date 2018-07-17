package com.bigdata.xwork.action.actions;

import com.banggood.xwork.action.core.*;
import com.bigdata.xwork.action.impl.ShellAction;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.core.service.WorkFlowService;
import com.bigdata.xwork.dao.entity.UserInfo;
import com.bigdata.xwork.dao.service.WorkDaoService;
import com.bigdata.xwork.action.core.*;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("ALL")
public class ShellActionTest extends BaseTestApplication {

    private static String workFlowName = "shell-work-flow";
    private final String command = "/bin/bash -c \"for((i=1;i<=10;i++));do echo \\$i;sleep 1;done\"";

    @Autowired
    WorkDaoService workDaoService;
    @Autowired
    WorkFlowService workFlowService;

    public static ShellAction getShellAction(String actionName) {
        ShellAction action = new ShellAction();
        action.setActionName(actionName);

        return action;
    }


    @Test
    public void testShellAction()
            throws ParamDataTypeException, NoParamException, ParseActionException, UnknownHostException, InterruptedException, CloneNotSupportedException {
        Map<String, String> maps = new HashMap<>();
        maps.put("path", "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin:/bin:/Users/zouyi/Library/Maven/apache-maven-3.5.0/bin:/Users/zouyi/Library/module/mysql-5.7.13-osx10.11-x86_64/bin:/Users/zouyi/Downloads/gradle-3.5.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");
        if (!true) {
            WorkFlow workFlow = new WorkFlow();
            workFlow.setFlowName("shell-work-flow4");
            /**
             * 保存workFlow配置的时候一定要配
             */
            workFlow.putToConfig("runTime", String.valueOf(System.currentTimeMillis()));


            ShellAction action_1 = getShellAction("shell_action_1");
            ShellAction action_2 = getShellAction("shell_action_2");
            ShellAction action_3 = getShellAction("shell_action_3");
            ShellAction action_4 = getShellAction("shell_action_4");

//         Map<String, String> confEnvMap = new HashMap<>();
//         confEnvMap.put("path", "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin:/bin:/Users/zouyi/Library/Maven/apache-maven-3.5.0/bin:/Users/zouyi/Library/module/mysql-5.7.13-osx10.11-x86_64/bin:/Users/zouyi/Downloads/gradle-3.5.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");

//         workFlow.getRunParam().putMap(ShellAction.CONF_XWORK_SHELL_ENV, confEnvMap);
//         workFlow.getRunParam().putString(ShellAction.CONF_XWORK_SHELL_COMMAND, "ls -lh;whoami");
//         workFlow.setWorkDaoService(workDaoService);

            workFlow.initialize();

            workFlow.appendLeaf(WorkActionConstant.STARTACTION, action_1);


            workFlow.appendLeaf("shell_action_1", action_2);

            workFlow.appendLeaf("shell_action_1", action_3);

            workFlow.appendLeaf("shell_action_2", action_4);

            workFlow.appendLeaf("shell_action_3", action_4);


            List<WorkActionBase> actions = workFlow.getActions();

            for (WorkActionBase action : actions) {
                if (action instanceof ShellAction) {
                    ShellAction demo = (ShellAction) action;

//                    demo.setShellActionConfig(command, maps);
                }

            }
//            CountDownLatch count = new CountDownLatch(1);
//
//            workFlow.getEndWorkAction().setWorkFlowService(workFlowService);
            workFlow.build();
//            workFlow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {
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
//
//            });
//            workFlow.start();
//            count.await(200000, TimeUnit.MILLISECONDS);
            UserInfo user = this.workDaoService.getUserInfoByName("mxq");
            this.workDaoService.addWorkFlow(workFlow, user);

        } else {
            CountDownLatch count = new CountDownLatch(1);

            WorkFlow workFlow = this.workDaoService.getWorkFlow("shell-work-flow");
            /**
             * 在getWorkFlow这个方法里面,已经设置了setWorkDaoService()
             */
            workFlow.initialize();

            /**
             * 保存workFlow_Instance时必须要的参数
             */
//            Map<String, String> confEnvMap = new HashMap<>();
//            confEnvMap.put("path", "/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/bin:/bin:/Users/zouyi/Library/Maven/apache-maven-3.5.0/bin:/Users/zouyi/Library/module/mysql-5.7.13-osx10.11-x86_64/bin:/Users/zouyi/Downloads/gradle-3.5.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");
//            workFlow.getRunParam().putMap(ShellAction.CONF_XWORK_SHELL_ENV, confEnvMap);
//            workFlow.getRunParam().putString(ShellAction.CONF_XWORK_SHELL_COMMAND, "/bin/bash -c \"for((i=1;i<=10;i++));do echo \\$i;sleep 1;done\"");

            workFlow.setWorkDaoService(workDaoService);
            workFlow.getEndWorkAction().setWorkFlowService(workFlowService);
            workFlow.build();
            workFlow.getEndWorkAction().setCallBack(count, new WorkRunCallBack() {

                @Override
                public void listen(WorkActionBase action) {

                    count.countDown();

                }

                @Override
                public void initialize() {

                }

            });
            workFlow.start();

            count.await(200000, TimeUnit.MILLISECONDS);

            WorkRunStatus status = workFlow.getStatus();

            Assert.assertTrue("Linear Topology work flow may be is running or it has been failed",
                    status == WorkRunStatus.SUCCESS);

        }
    }
}
