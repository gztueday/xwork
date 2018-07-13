package com.banggood.xwork.scheduler.core.submit;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkFlow;
import com.banggood.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.entity.WorkSchedulerInstance;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.dubb.controller.WebExecutorController;
import com.banggood.xwork.scheduler.core.WorkScheduler;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

/**
 * quartz必须要加@Component注解,但是spring的属性注入不了(按理来说不应该这样的)
 * schedulerRelations只管你自己那一行的版本就好
 */
@Component
public class DispathcerSchedulerSubmiter implements Job {

    private final static Logger logger = LoggerFactory.getLogger(DispathcerSchedulerSubmiter.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        WorkDaoService workDaoService = (WorkDaoService) WebExecutorController.beanMaps.get("com.banggood.xwork.dao.service.workDaoService");
        WorkSchedulerService workSchedulerService = (WorkSchedulerService) WebExecutorController.beanMaps.get("com.banggood.xwork.scheduler.core.service.workSchedulerService");

        String schedulerInstanceId = context.getJobDetail().getJobDataMap().getString("key");

        WorkScheduler workScheduler = (WorkScheduler) context.getJobDetail().getJobDataMap().get(schedulerInstanceId);

        WorkFlow workFlow;
        try {
            workFlow = workDaoService.getWorkFlow(workScheduler.getFlowName());
            workFlow.initialize();
            workFlow.setStatus(WorkRunStatus.DISTRIBUTED);
            workFlow.setRunTime(System.currentTimeMillis());
            workFlow.setStartTime(System.currentTimeMillis());
            Map<String, Map<String, String>> parameters = workScheduler.getArguments();

            workFlow.build();
            if (parameters != null && parameters.size() > 0) {
                for (String actionName : parameters.keySet()) {
                    WorkActionBase action = workFlow.getWorkActionByName(actionName);
                    Map<String, String> argumentMaps = parameters.get(actionName);
                    action.putActionsArguments(argumentMaps);
                    logger.info(" putActionsArguments: {} ", argumentMaps);
                }
                workFlow.setArguments(parameters);
            }
            workFlow.setSchedulerid(schedulerInstanceId);
            logger.info("workFlow: [{}]", workFlow.toString());
            /**
             * 每创建一个SchedulerInstance实例需要与workFlowInstance建立关系
             */
            if (workSchedulerService.queryCountForDispatcher(schedulerInstanceId) <= 0) {
                logger.info("所有的版本都已经运行了");
                closeScheduler(schedulerInstanceId);
            } else {
                /**
                 * 检查版本次数
                 */
                long version = workSchedulerService.queryMinVersionForDispatcher(schedulerInstanceId);
                WorkSchedulerInstance schedulerInstance = workSchedulerService.querySchedulerByInstanceidAndVersion(schedulerInstanceId, version);
                String paramsJson = schedulerInstance.getParamsJson();
                if (paramsJson != null) {
                    schedulerInstance.parseRunParam(JSONObject.parseArray(paramsJson), workFlow);
                }
                long maxVersion = workSchedulerService.queryMaxVersionForDispatcher(schedulerInstanceId);
                checkSchedulerInstanceNum(workScheduler, workSchedulerService, schedulerInstanceId, maxVersion);
                workSchedulerService.updateWorkFlowRelationIntoSchedulerStatus(schedulerInstanceId, workFlow.getInstanceid(), version, new Date());
                workDaoService.addWorkFlowInstance(workFlow, workFlow.getInstanceid());
            }
        } catch (ParseActionException | CloneNotSupportedException | SchedulerException
                | UnknownHostException | NoParamException | ParamDataTypeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void checkSchedulerInstanceNum(WorkScheduler workScheduler, WorkSchedulerService workSchedulerService, String schedulerInstanceId, long version)
            throws SchedulerException {

        int count = workSchedulerService.queryDispatcherCount(schedulerInstanceId);
        Date date = workSchedulerService.selectExecuteDate(schedulerInstanceId);
        CronSequenceGenerator generator = new CronSequenceGenerator(workScheduler.getCronStr());
        System.out.println(" workScheduler.getEndTime(): " + workScheduler.getEndTime());
        Date next = generator.next(date);
        System.out.println(" generator.next(date).getTime(): " + next.getTime());
        if (count < 20) {
            for (int i = 0; i < 20 - count; i++) {
                if (workScheduler.getEndTime() == 0 || (workScheduler.getEndTime() != 0 && workScheduler.getEndTime() > next.getTime())) {
                    /**
                     * 更新date
                     */
                    workScheduler.setDate(next);
                    logger.info(" 添加DISTRIBUTED状态的实例以保持数据库中有20条DISTRIBUTED状态的scheduler实例 ");
                    workSchedulerService.addSchedulerInstance(workScheduler, version);
                }
                Date minDate = workSchedulerService.selectExecuteMinDate(schedulerInstanceId);
                Date minTime = generator.next(minDate);
                if (workScheduler.getEndTime() != 0 && workScheduler.getEndTime() < minTime.getTime()) {
                    logger.info(" 定时任务{}到结束时间了 ", schedulerInstanceId);
                    closeScheduler(schedulerInstanceId);
                    workSchedulerService.updateSchedulerStatusGroup(schedulerInstanceId, WorkRunStatus.SUCCESS);
                }
            }
        }
    }

    private void closeScheduler(String schedulerInstanceId) throws SchedulerException {
        JobKey jobKey = WorkSchedulerService.jobKeyMap.get(schedulerInstanceId);
        Scheduler scheduler = WorkSchedulerService.schedulerMap.get(schedulerInstanceId);
        if (jobKey != null && scheduler != null) {
            scheduler.deleteJob(jobKey);
            WorkSchedulerService.jobKeyMap.remove(schedulerInstanceId);
            WorkSchedulerService.schedulerMap.remove(schedulerInstanceId);
        }
    }
}
