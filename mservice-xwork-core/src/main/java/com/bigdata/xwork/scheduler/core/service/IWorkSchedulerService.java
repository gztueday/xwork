package com.bigdata.xwork.scheduler.core.service;

import com.bigdata.xwork.query.SchedulerInstanceQueryObject;
import com.bigdata.xwork.query.result.PageResult;
import com.bigdata.xwork.scheduler.core.WorkScheduler;

import java.util.Date;

/**
 * Created by zouyi on 2018/1/21.
 */
public interface IWorkSchedulerService  {

    boolean addWorkScheduler(WorkScheduler scheduler);

    void updateSchedulerInstanceStatusForSuccess(String relationid, String flowName, Date date);

    PageResult queryForSchedulerInstance(SchedulerInstanceQueryObject qo);
}
