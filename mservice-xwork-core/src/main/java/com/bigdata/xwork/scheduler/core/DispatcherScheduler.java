package com.bigdata.xwork.scheduler.core;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.bigdata.xwork.action.core.SeekOut;
import com.bigdata.xwork.action.core.WorkActionRelation;
import com.bigdata.xwork.action.core.WorkFlow;
import com.bigdata.xwork.core.common.DependencyWorkAction;
import com.bigdata.xwork.core.common.RemoteWorkAction;
import com.bigdata.xwork.core.common.ServiceRegistry;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.core.service.WorkFlowService;
import com.bigdata.xwork.dao.entity.Bundle;
import com.bigdata.xwork.dao.entity.SchedulerRemoteAction;
import com.bigdata.xwork.dao.entity.WorkSchedulerInstance;
import com.bigdata.xwork.dao.mapper.SchedulerConfigBundleMapper;
import com.bigdata.xwork.dao.mapper.SchedulerConfigMapper;
import com.bigdata.xwork.dao.mapper.SchedulerInstanceMapper;
import com.bigdata.xwork.dao.service.WorkDaoService;
import com.bigdata.xwork.query.AcceptSchedulerObject;
import com.bigdata.xwork.scheduler.core.service.WorkSchedulerService;
import com.bigdata.xwork.action.core.WorkRunStatus;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zouyi on 2018/1/15.
 */
@Component
public class DispatcherScheduler {

    @Autowired
    private ServiceRegistry registry;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerConfigMapper schedulerConfigMapper;
    @Autowired
    private WorkSchedulerService workSchedulerService;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerConfigBundleMapper schedulerConfigBundleMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerInstanceMapper schedulerInstanceMapper;
    @Autowired
    private SeekOut seekOut;
    @Autowired
    private WorkDaoService workDaoService;

    private final static Logger logger = LoggerFactory.getLogger(DispatcherScheduler.class);
    /**
     * 定时任务执行线程是不会阻塞的
     * <p>
     * super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
     * new DelayedWorkQueue());
     */
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);


    /**
     * 保存的时候把实例和配置一起保存
     *
     * @param acceptSchedulerObject
     */
    public void insertScheduler(AcceptSchedulerObject acceptSchedulerObject) throws ParseException {
        logger.info("定时任务配置保存");
        new CronExpression(acceptSchedulerObject.getCron());
        long dependVersion;
        if (this.workSchedulerService.queryCount(acceptSchedulerObject.getSchedulerName()) > 0) {
            AcceptSchedulerObject asoCopy = this.workSchedulerService.querySchedulerConfig(acceptSchedulerObject.getSchedulerName());
            dependVersion = asoCopy.getDependVersion();
            acceptSchedulerObject.setCreateTime(asoCopy.getCreateTime());
            this.workSchedulerService.deleteByName(acceptSchedulerObject.getSchedulerName());
        } else {
            acceptSchedulerObject.setCreateTime(new Date());
            dependVersion = System.currentTimeMillis();
        }
        acceptSchedulerObject.setDependVersion(dependVersion);
        acceptSchedulerObject.setUpdateTime(new Date());
        this.schedulerConfigMapper.insert(acceptSchedulerObject);
        List<Bundle> bundles = acceptSchedulerObject.getBundles();
        this.schedulerConfigBundleMapper.delete(acceptSchedulerObject.getSchedulerName());
        if (bundles != null) {
            for (Bundle bundle : bundles) {
                if (bundle.getRemoteInstanceid() != null && !"".equals(bundle.getRemoteInstanceid())) {
                    bundle.setCalculate(bundle.getVersion() - acceptSchedulerObject.getDependVersion());
                    this.schedulerConfigBundleMapper.insert(bundle);
                }
            }
        }
    }

    /**
     * scheduler开始运行任务方法
     *
     * @param scheduler
     * @param paramsJson
     * @throws ParseActionException
     * @throws NoParamException
     * @throws ParamDataTypeException
     */
    public void start(String scheduler, String paramsJson) throws ParseActionException, NoParamException, ParamDataTypeException, ParseException {
        try {
            boolean option = false;
            AcceptSchedulerObject acceptSchedulerObject = this.workSchedulerService.querySchedulerConfig(scheduler);
            Date nextTimePoint = null;
            String executeIp = registry.discover();
            String workFlowName = acceptSchedulerObject.getWorkFlowName();
            String cron = acceptSchedulerObject.getCron();
            Timestamp startTime = null;
            Date startDate = acceptSchedulerObject.getStartDate();
            if (startDate != null) {
                long time = startDate.getTime();
                startTime = new Timestamp(time);
                nextTimePoint = new Date(time);
            } else {
                nextTimePoint = new Date();
            }
            String schedulerInstance = createSchedulerInstance();
            String schedulerName = acceptSchedulerObject.getSchedulerName();
            Date endDate = acceptSchedulerObject.getEndDate();
            Timestamp endTime = null;
            if (endDate != null) {
                endTime = new Timestamp(endDate.getTime());
            }
            long version = acceptSchedulerObject.getDependVersion();
            Date date = null;
            String rightRelationsJson = null;
            String cacheRelationsJson = null;
            /**
             * 每保存一次都更新关系,但是保存的时候就已经把关系设置好了,保存的那20次的关系是一样的
             */
            if (acceptSchedulerObject.getBundles() != null && acceptSchedulerObject.getBundles().size() > 0) {
                /**
                 * 以dependAction为准,有依赖的时候才创建这个map
                 */
                Map<String, WorkActionRelation> rightRelationsMap = new HashMap<>(5);
                Map<String, WorkActionRelation> cacheMap = new HashMap<>(5);
                for (Bundle bundle : acceptSchedulerObject.getBundles()) {
                    /**
                     * 右边scheduler实例的workFlow名称,左边的scheduler实例是设置remoteAction
                     */
                    String wfName = this.workSchedulerService.queryWorkFlowName(bundle.getRemoteInstanceid());
                    WorkActionRelation workActionRelation = new WorkActionRelation();
                    String[] split = bundle.getRemoteActionStr().split(",");
                    List<DependencyWorkAction> dependencyWorkActionList = new ArrayList<>(split.length);
                    for (String remoteAction : split) {
                        DependencyWorkAction dependencyAction = new DependencyWorkAction();
                        dependencyAction.setRemoteName(remoteAction);
                        dependencyAction.setWorkFlowName(wfName);
                        dependencyAction.setWorkActionName(bundle.getDependAction());
                        dependencyAction.setSchedulerid(bundle.getRemoteInstanceid());
                        dependencyWorkActionList.add(dependencyAction);
                    }
                    workActionRelation.setDependWorkActions(dependencyWorkActionList);
                    rightRelationsMap.put(bundle.getDependAction(), workActionRelation);
                    /**
                     * 以下是更改左边在运行中实例的关系
                     */
                    WorkSchedulerInstance leftSchedulerInstance = this.workSchedulerService.querySchedulerByInstanceidAndVersion(bundle.getRemoteInstanceid(),
                            seekOut.calculatoRemoteVersion(version, bundle.getCalculate()));
                    Map<String, WorkActionRelation> map;
                    if (leftSchedulerInstance != null) {
                        String relationsJson = leftSchedulerInstance.getRelationsJson();
                        if (relationsJson != null && !"{\"\":{\"children\":[],\"dependWorkActions\":[{\"remoteDependName\":\"\",\"remoteName\":\"\",\"remoteSwitch\":false,\"schedulerid\":0}],\"fathers\":[],\"remoteDepends\":[]}}".equals(relationsJson)) {
                            map = JSONObject.parseObject(relationsJson, new TypeReference<Map<String, WorkActionRelation>>() {
                            });
                            for (String remoteAction : split) {
                                RemoteWorkAction remoteWorkAction = new RemoteWorkAction();
                                remoteWorkAction.setDependWorkFlowName(workFlowName);
                                remoteWorkAction.setRemoteActionName(remoteAction);
                                remoteWorkAction.setCalculate(bundle.getCalculate());
                                remoteWorkAction.setRemoteDependName(bundle.getDependAction());
                                remoteWorkAction.setDependSchedulerInstanceid(schedulerInstance);
                                WorkActionRelation param = map.get(remoteAction);
                                if (param == null) {
                                    param = new WorkActionRelation();
                                }
                                param.setActionName(remoteAction);
                                param.getRemoteDepends().add(remoteWorkAction);
                                map.put(remoteAction, param);
                            }
                        } else {
                            map = new HashMap<>(split.length);
                            for (String remoteAction : split) {
                                WorkActionRelation wa = new WorkActionRelation();
                                List<RemoteWorkAction> remoteWorkActions = new ArrayList<>();
                                RemoteWorkAction remoteWorkAction = new RemoteWorkAction();
                                wa.setActionName(remoteAction);
                                wa.setRemoteDepends(remoteWorkActions);
                                remoteWorkActions.add(remoteWorkAction);
                                remoteWorkAction.setDependWorkFlowName(workFlowName);
                                remoteWorkAction.setRemoteActionName(remoteAction);
                                remoteWorkAction.setCalculate(bundle.getCalculate());
                                remoteWorkAction.setRemoteDependName(bundle.getDependAction());
                                remoteWorkAction.setDependSchedulerInstanceid(schedulerInstance);
                                map.put(remoteAction, wa);
                            }
                        }
                        for (int i = 0; i < this.workSchedulerService.querySchedulerInstanceCount(bundle.getRemoteInstanceid()); i++) {
                            this.workSchedulerService.updateRelations(bundle.getRemoteInstanceid(),
                                    seekOut.calculatoRemoteVersion(version + i, bundle.getCalculate()),
                                    JSONObject.toJSONString(map));
                        }
                        copyRelationsMap(rightRelationsMap, cacheMap);
                        if (leftSchedulerInstance.getStatus().equals(WorkRunStatus.RUNNING)) {
                            for (String remoteAction : split) {
                                SchedulerRemoteAction schedulerRemoteAction = new SchedulerRemoteAction();
                                schedulerRemoteAction.setWfInstance(leftSchedulerInstance.getWfInstanceid());
                                schedulerRemoteAction.setDependSchedulerInstanceid(schedulerInstance);
                                schedulerRemoteAction.setDependSchedulerInstanceVersion(version);
                                schedulerRemoteAction.setRemoteAction(remoteAction);
                                schedulerRemoteAction.setDependAction(bundle.getDependAction());
                                schedulerRemoteAction.setDependWorkFlowName(workFlowName);
                                schedulerRemoteAction.setCalculate(bundle.getCalculate());
                                ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("bundleInMemory");
                                ref.setUrl(leftSchedulerInstance.getExecuteIp());
                                String result = ref.get().bundleInMemory(JSONObject.toJSONString(schedulerRemoteAction));
                                logger.info("在内存中运行: {}", result);
                                if ("SUCCESS".equals(result)) {
                                    cacheMap.remove(bundle.getDependAction());
                                    option = true;
                                }
                            }
                        }
                    }
                }
                rightRelationsJson = JSONObject.toJSONString(rightRelationsMap);
                cacheRelationsJson = JSONObject.toJSONString(cacheMap);
            }

            for (int i = 0; i < 20; i++) {
                /**
                 * 设置右边的scheduler实例
                 */
                //---------------------------------------------------------------------
                WorkSchedulerInstance rightInstance = new WorkSchedulerInstance();
                rightInstance.setSchedulerStatus(WorkRunStatus.RUNNING);
                CronExpression exp = new CronExpression(acceptSchedulerObject.getCron());
                nextTimePoint = exp.getNextValidTimeAfter(nextTimePoint);
                if (i == 0) {
                    date = nextTimePoint;
                }
                rightInstance.setDate(nextTimePoint);
                rightInstance.setStatus(WorkRunStatus.DISTRIBUTED);
                rightInstance.setStartTime(new Timestamp(nextTimePoint.getTime()));
                rightInstance.setEndTime(endTime);
                rightInstance.setParamsJson(paramsJson);
                rightInstance.setVersion(version + i);
                rightInstance.setInstanceid(schedulerInstance);
                rightInstance.setCronStr(cron);
                rightInstance.setFlowName(workFlowName);
                rightInstance.setSchedulerName(schedulerName);
                rightInstance.setCreateTime(new Date());

                rightInstance.setExecuteIp(executeIp);

                //------------------------------------------------------------------------------------------------------------------------------------------------
                if (rightRelationsJson != null) {
                    rightInstance.setRelationsJson(rightRelationsJson);
                }
                if (option && !"{}".equals(cacheRelationsJson)) {
                    rightInstance.setRelationsJson(cacheRelationsJson);
                }
                if (endTime != null && endTime.getTime() < nextTimePoint.getTime()) {
                    break;
                }
                this.schedulerInstanceMapper.insert(rightInstance);
                //------------------------------------------------------------------------
            }
            //------------------------------------------------------------------------
            /**
             * 分发出去开始执行
             */
            ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("dispatcherScheduler");
            ref.setUrl(executeIp);
            WorkScheduler workScheduler = new WorkScheduler();
            workScheduler.setFlowName(workFlowName);
            workScheduler.setCronStr(cron);
            workScheduler.setArguments(acceptSchedulerObject.getArguments());
            workScheduler.setSchedulerName(schedulerName);
            if (endTime != null) {
                workScheduler.setEndTime(endTime.getTime());
            }
            workScheduler.setInstanceid(schedulerInstance);
            workScheduler.setVersions(version);
            workScheduler.setDate(date);
            if (startTime != null) {
                long startTimeTime = startTime.getTime();
                workScheduler.setStartTime(startTimeTime);
                if (startTimeTime > System.currentTimeMillis()) {
                    long intervalTime = startTimeTime - System.currentTimeMillis();
                    scheduledExecutorService.schedule(() -> {
                        logger.info("间隔时间为:[{}]", intervalTime);
                        logger.info("定时任务按照[{}]时间启动", startDate);
                        ref.get().dispatcherScheduler(JSONObject.toJSONString(workScheduler));

                    }, intervalTime, TimeUnit.MILLISECONDS);
                } else {
                    ref.get().dispatcherScheduler(JSONObject.toJSONString(workScheduler));
                }
            } else {
                ref.get().dispatcherScheduler(JSONObject.toJSONString(workScheduler));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //------------------------------------------------------------------------
    }

    public void copyRelationsMap(Map<String, WorkActionRelation> rightRelationsMap, Map<String, WorkActionRelation> cacheMap) {
        for (String key : rightRelationsMap.keySet()) {
            cacheMap.put(key, rightRelationsMap.get(key));
        }
    }

    public String createSchedulerInstance() {
        return new StringBuilder(50).append("scheduler_").append(UUID.randomUUID().toString())
                .append("_num_").append(System.currentTimeMillis()).toString();

    }

    public EventResponse dokill(String schedulerInstanceid) {
        EventResponse eventResponse = new EventResponse();
        try {
            List<WorkSchedulerInstance> instances = this.schedulerInstanceMapper.queryLatestRunningSchedulerInstance(schedulerInstanceid);
            if (instances != null && instances.size() > 0) {
                for (WorkSchedulerInstance instance : instances) {
                    /**
                     * 杀死定时任务
                     */
                    ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("schedulerDoKill");
                    ref.setUrl(instance.getExecuteIp());
                    ref.get().schedulerDoKill(JSONObject.toJSONString(instance));
                    this.workDaoService.doKill(instance.getWfInstanceid());
                    eventResponse.setStatus(HttpServletResponse.SC_OK);
                    eventResponse.setDesc(" do kill success ");
                }
            } else {
                WorkSchedulerInstance schedulerInstance = this.schedulerInstanceMapper.queryDispatcherScheduler(schedulerInstanceid);
                if (schedulerInstance.getStatus().equals(WorkRunStatus.DISTRIBUTED)) {
                    eventResponse.setDesc(" 此定时任务实例是DISTRIBUTED的状态,无法进行kill操作 ");
                    eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return eventResponse;
                }
                ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("schedulerDoKill");
                ref.setUrl(schedulerInstance.getExecuteIp());
                ref.get().schedulerDoKill(JSONObject.toJSONString(schedulerInstance));
            }
            this.schedulerInstanceMapper.updateSchedulerStatusGroup(schedulerInstanceid, WorkRunStatus.KILLED);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            eventResponse.setDesc(" do kill error ");
        }
        return eventResponse;
    }

    public String deleteSchedulerConfig(String[] delete) {
        for (int i = 0; i < delete.length; i++) {
            if (!this.schedulerConfigMapper.deleteSchedulerConfig(delete[i])) {
                return " 删除失败 ";
            }
        }
        return " 删除成功 ";
    }

    public EventResponse resume(String schedulerInstanceid) {
        EventResponse eventResponse = new EventResponse();
        List<WorkSchedulerInstance> instanceList = this.schedulerInstanceMapper.querySchedulerByRunning(schedulerInstanceid);
        for (WorkSchedulerInstance instance : instanceList) {
            String wfInstanceid = instance.getWfInstanceid();
            if (wfInstanceid == null) {
                eventResponse.setDesc(" this workFlow " + instance.getWfInstanceid() + " is null ");
                eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return eventResponse;
            } else {
                WorkFlow workFlowInstance = this.workFlowService.getWorkFlowInstance(wfInstanceid);
                if (workFlowInstance != null) {
                    String ip = workFlowInstance.getIp();
                    ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("schedulerResume");
                    ref.setUrl(ip);
                    eventResponse = ref.get().schedulerResume(workFlowInstance.getInstanceid());
                    if (eventResponse.getStatus() == HttpServletResponse.SC_BAD_REQUEST) {
                        return eventResponse;
                    }
                }
            }
        }
        eventResponse.setStatus(HttpServletResponse.SC_OK);
        eventResponse.setDesc(" 重启成功 ");
        return eventResponse;
    }

}

