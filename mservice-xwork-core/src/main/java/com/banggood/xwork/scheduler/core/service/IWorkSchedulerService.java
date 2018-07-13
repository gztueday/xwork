package com.banggood.xwork.scheduler.core.service;

import com.banggood.xwork.query.SchedulerInstanceQueryObject;
import com.banggood.xwork.query.result.PageResult;
import com.banggood.xwork.scheduler.core.WorkScheduler;

import java.util.Date;

/**
 * Created by zouyi on 2018/1/21.
 */
public interface IWorkSchedulerService  {

    boolean addWorkScheduler(WorkScheduler scheduler);

    void updateSchedulerInstanceStatusForSuccess(String relationid, String flowName, Date date);

    PageResult queryForSchedulerInstance(SchedulerInstanceQueryObject qo);
}
