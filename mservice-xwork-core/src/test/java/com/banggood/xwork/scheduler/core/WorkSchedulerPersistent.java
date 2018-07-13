package com.banggood.xwork.scheduler.core;

import com.banggood.xwork.action.core.BaseTestApplication;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.UnknownHostException;


@SuppressWarnings("ALL")
public class WorkSchedulerPersistent extends BaseTestApplication {

    @Autowired
    WorkDaoService workDaoService;
    @Autowired
    WorkSchedulerService workSchedulerService;

    /**
     * work_flow_scheduler表的持久化
     *
     * @throws NoParamException
     * @throws ParamDataTypeException
     * @throws ParseActionException
     * @throws CloneNotSupportedException
     * @throws InterruptedException
     * @throws UnknownHostException
     */
    @Test
    public void testWorkScheduler()
            throws NoParamException, ParamDataTypeException, ParseActionException, CloneNotSupportedException, InterruptedException, UnknownHostException {
        WorkScheduler workScheduler = new WorkScheduler();
        workScheduler.setConfigerid(2);
        workScheduler.setCronStr("0/5 * * * * ? ");
        workScheduler.setFlowName("test-work");
        workScheduler.setSchedulerName("test-scheduler");
        workScheduler.putToConfig("runTime", String.valueOf(System.currentTimeMillis()));
        workScheduler.initialize();
        workScheduler.setSubmiter(2);
        workScheduler.setUpdaterid(2);
        boolean status = this.workSchedulerService.addWorkScheduler(workScheduler);
        Assert.assertTrue("add new WorkScheduler success", status);

    }
}