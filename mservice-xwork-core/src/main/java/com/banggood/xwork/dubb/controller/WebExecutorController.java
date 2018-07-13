package com.banggood.xwork.dubb.controller;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.banggood.xwork.api.entity.LineResult;
import com.banggood.xwork.core.common.*;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.exception.RemoteProcedureException;
import com.banggood.xwork.core.service.ExecutorDispatcher;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.banggood.xwork.dao.mapper.SchedulerInstanceMapper;
import com.banggood.xwork.dao.mapper.WorkActionInstanceMapper;
import com.banggood.xwork.dao.mapper.WorkFlowInstanceMapper;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.scheduler.core.WorkScheduler;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import com.banggood.xwork.scheduler.core.submit.DispathcerSchedulerSubmiter;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 监听web发过来的信息,从而对core执行对应的操作
 */
@com.alibaba.dubbo.config.annotation.Service(version = "1.0.0", timeout = 10000, interfaceName = "com.banggood.xwork.api.entity.IWebExecutorController")
public class WebExecutorController implements IWebExecutorController {

    private final static Logger logger = LoggerFactory.getLogger(WebExecutorController.class);

    @Autowired
    private WorkFlowConfigProperty wfcp;
    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkSchedulerService workSchedulerService;
    @Autowired
    private WorkDaoService workDaoService;
    @Autowired
    private WorkFlowInstanceMapper workFlowInstanceMapper;
    @Autowired
    private FlowActionUtils flowActionUtils;
    @Autowired
    private ExecutorDispatcher executorDispatcher;
    @Value("${spring.dubbo.protocol.port}")
    private String port;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkActionInstanceMapper workActionInstanceMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerInstanceMapper schedulerInstanceMapper;

    public static Map<String, Object> beanMaps = new HashMap<>(3);

//    @Override
//    public EventResponse schedulerRemoteRelation(String workFlowInfo, String instanceid) {
//        EventResponse eventResponse = new EventResponse();
//        WorkFlowInfo info = JSONObject.parseObject(workFlowInfo, WorkFlowInfo.class);
//        JSONObject object = JSONObject.parseObject(info.getRelationsJson());
//        WorkFlow wf = WorkFlowService.cachedFlowInstance.get(instanceid);
//        Set<String> keySet = object.keySet();
////      Map<String, WorkActionRelation> map = new HashMap<>(keySet.size());
//        for (String key : keySet) {
//            WorkActionRelation param = object.getObject(key, WorkActionRelation.class);
//            List<RemoteWorkAction> remoteDepends = param.getRemoteDepends();
//            if (remoteDepends != null && remoteDepends.size() > 0) {
//                for (RemoteWorkAction remoteDepend : remoteDepends) {
//                    WorkActionBase workActionBase = wf.getWorkActionByName(remoteDepend.getRemoteActionName());
//                    workActionBase.getRemoteDepend().add(remoteDepend);
//                }
//            }
//        }
//
//        return eventResponse;
//
//    }

    /**
     * 内部执行远程调用接口
     *
     * @param workFlowString
     * @return
     */
    @Override
    public EventResponse remote(String workFlowString) {
        EventResponse eventResponse = new EventResponse();
        try {
            WorkFlow wf = parseWorkFlow(workFlowString);
            //WorkFlow wf = JSONObject.parseObject(workFlowString, WorkFlow.class);
            wf.initialize();
            wf.setWorkSchedulerService(this.workSchedulerService);
            wf.setWorkDaoService(this.workDaoService);
            wf.getEndWorkAction().setWorkFlowService(this.workFlowService);
            wf.buildForWorkFlowInstance();
            executorDispatcher.setWorkFlowEndaction(wf);
            wf.setOutputStream(new FileOutputStream(wfcp.getLogPath() + wf.getInstanceid() + ".txt"));
            this.workFlowService.addWorkFlowInstance(wf);
            wf.startlocalWorkFlowInstance();
            wf.setStatus(WorkRunStatus.RUNNING);

            wf.setIp(this.serviceRegistry.getDubbopIP());
            // wf.setIp(InetAddress.getLocalHost().getAddress().toString()+":"+port);
            //this.workDaoService.addWorkFlowInstance(wf);
            this.workDaoService.addWorkFlowInstanceByRemote(wf);
            String desc = "remote call " + wf.getFlowName() + " has been succeed";
            logger.info(desc);
            eventResponse.setDesc(desc);
            eventResponse.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            eventResponse.setDesc("remote call  has been fail" + e.toString());
            logger.error(e.toString());
        }
        return eventResponse;
    }

    @Override
    public LineResult querySchedulerLogger(String schedulerInstance, long size) {
        List<String> content = new ArrayList<>();
        LineResult lineResult = new LineResult();
        try {
            String fileName = wfcp.getSchedulerlog() + schedulerInstance + ".txt";
            File file = new File(fileName);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(fileName));//构造一个BufferedReader类来读取文件
                String s = null;
                int lines = 0;
                //使用readLine方法，一次读一行
                while ((s = br.readLine()) != null) {
                    lines = lines + 1;
                    if (size <= lines) {
                        size = size + 1;
                        content.add(s);
                    }
                }
                br.close();
                lineResult.setContent(content);
                lineResult.setLine(size);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lineResult;
    }

    private WorkFlow parseWorkFlow(String workFlowString) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        WorkFlow wf = new WorkFlow();
        JSONObject jsonObject = JSONObject.parseObject(workFlowString);
        //workFlow的普通属性
        wf.setInstanceid(jsonObject.getString("instanceid"));
        wf.setUuid(jsonObject.getString("uuid"));
        wf.setFlowName(jsonObject.getString("flowName"));
        wf.setStartTime(jsonObject.getLong("startTime"));
        wf.setRunTime(jsonObject.getLong("runTime"));
        wf.setStatus(WorkRunStatus.DISTRIBUTED);
        wf.setSchedulerid(jsonObject.getString("schedulerid"));
        wf.setConfigerid(jsonObject.getLong("configerid"));
        wf.setIp(jsonObject.getString("ip"));
        wf.setTyrTime(jsonObject.getInteger("tyrTime"));
        List<RemoteWorkFlow> remoteWorkFlows = JSONObject.parseArray(jsonObject.getString("remoteWorkFlows"), RemoteWorkFlow.class);
        wf.setRemoteWorkFlows(remoteWorkFlows);
        //startWorkAction
        String startWorkActionString = jsonObject.get("startWorkAction").toString();
        StartWorkAction startWorkAction = JSONObject.parseObject(startWorkActionString, StartWorkAction.class);
        startWorkAction.setInstanceid(JSONObject.parseObject(startWorkActionString).get("instanceid").toString());
        wf.setStartWorkAction(startWorkAction);
        //endWorkActions
        String endWorkActionString = jsonObject.get("endWorkAction").toString();
        EndWorkAction endWorkAction = JSONObject.parseObject(endWorkActionString, EndWorkAction.class);
        endWorkAction.setInstanceid(JSONObject.parseObject(endWorkActionString).get("instanceid").toString());
        wf.setEndWorkAction(endWorkAction);
        //relations
        Map<String, WorkActionRelation> relations = jsonObject.getObject("relations", new TypeReference<Map<String, WorkActionRelation>>() {
        });
        wf.setRelations(relations);
        //actions
        JSONArray actions = jsonObject.getJSONArray("actions");
        List<WorkActionBase> workActionBases = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            JSONObject actionJson = actions.getJSONObject(i);

            Class<? extends WorkActionBase> cl = (Class<? extends WorkActionBase>) Class
                    .forName(actionJson.getString("clazz"));
            WorkActionBase action = cl.newInstance();
            //----------------------remote---------------------------
            JSONArray remoteDepends = actionJson.getJSONArray("remoteDepend");
            if (remoteDepends != null && remoteDepends.size() > 0) {
                for (int j = 0; j < remoteDepends.size(); j++) {
                    action.addRemoteDepend(remoteDepends.getObject(j, RemoteWorkAction.class));
                }
            }
            action.setConditionPass(actionJson.getBoolean("conditionPass"));
            action.setFormatName(actionJson.getString("formatName"));
            action.setSchedulerid(actionJson.getString("schedulerid"));
            action.setRunParam(actionJson.getObject("runParam", WorkConfig.class));
            action.setFlowName(actionJson.getString("flowName"));
            action.setUuid(actionJson.getString("uuid"));
            action.setActionType(actionJson.getObject("actionType", WorkActionType.class));
            action.setInstanceid(actionJson.getString("instanceid"));
            action.setShare(actionJson.getBoolean("share"));
            action.setStartTime(actionJson.getLong("startTime"));
            action.setRunTime(actionJson.getLong("runTime"));
            action.setConfig(actionJson.getObject("config", WorkConfig.class));
            action.setActionName(actionJson.getString("actionName"));
            action.setStatus(actionJson.getObject("status", WorkRunStatus.class));
            //-----------------------depend----------------------------//
            JSONArray dependendWorkActions = actionJson.getJSONArray("dependendWorkActions");
            List<DependencyWorkAction> dependencyWorkActions = new ArrayList<>();
            if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                for (int j = 0; j < dependendWorkActions.size(); j++) {
                    DependencyWorkAction dependencyWorkAction = dependendWorkActions.getObject(j, DependencyWorkAction.class);
                    dependencyWorkActions.add(dependencyWorkAction);
                }
                action.setDependendWorkActions(dependencyWorkActions);
            }
            workActionBases.add(action);
        }
        wf.setActions(workActionBases);
        wf.setConfig(jsonObject.getObject("config", WorkConfig.class));
        wf.setConfigerid(jsonObject.getLong("configerid"));
        wf.setStatus(jsonObject.getObject("status", WorkRunStatus.class));
        return wf;
    }

    @Override
    public String bundleInMemory(String schedulerRemoteActionJson) {
        SchedulerRemoteAction schedulerRemoteAction = JSONObject.parseObject(schedulerRemoteActionJson, SchedulerRemoteAction.class);
        WorkFlow wf = WorkFlowService.cachedFlowInstance.get(schedulerRemoteAction.getWfInstance());
        if (wf != null) {
            WorkSchedulerInstance instance = this.schedulerInstanceMapper.querySchedulerByInstanceidAndVersion(
                    schedulerRemoteAction.getDependSchedulerInstanceid(),
                    schedulerRemoteAction.getDependSchedulerInstanceVersion());
            WorkActionBase workAction = wf.getWorkActionByName(schedulerRemoteAction.getRemoteAction());
            if (workAction != null) {
                RemoteWorkAction remoteWorkAction = new RemoteWorkAction();
                if (instance != null && instance.getWfInstanceid() != null) {
                    remoteWorkAction.setIp(this.workDaoService.queryWorkFlowInstanceIp(instance.getWfInstanceid()));
                }
                if (!workAction.getStatus().equals(WorkRunStatus.SUCCESS)) {
                    remoteWorkAction.setDependWorkFlowName(schedulerRemoteAction.getDependWorkFlowName());
                    remoteWorkAction.setRemoteActionName(schedulerRemoteAction.getRemoteAction());
                    remoteWorkAction.setRemoteDependName(schedulerRemoteAction.getDependAction());
                    remoteWorkAction.setDependSchedulerInstanceid(schedulerRemoteAction.getDependSchedulerInstanceid());
                    remoteWorkAction.setCalculate(schedulerRemoteAction.getCalculate());
                    workAction.getRemoteDepend().add(remoteWorkAction);
                } else {
                    return "SUCCESS";
                }
            }
        }
        return null;
    }

    /**
     * 接收remoteAction请求的方法
     *
     * @param dependAction
     * @return
     */
    @Override
    public EventResponse dependAction(String dependAction) {
        WorkActionBase workAction;
        EventResponse eventResponse = new EventResponse();
        RemoteWorkAction remoteDepend = JSONObject.parseObject(dependAction, RemoteWorkAction.class);
        if (remoteDepend.getStatus() == WorkRunStatus.PENDING) {
            WorkFlow workFlow = WorkFlowService.pendingFlowInstance.get(remoteDepend.getWorkFlowInstanceid());
            if (workFlow != null) {
                workAction = workFlow.getWorkActionByName(remoteDepend.getRemoteDependName());
                List<DependencyWorkAction> dependencyWorkActions = workAction.getDependendWorkActions();
                for (DependencyWorkAction dependencyWorkAction : dependencyWorkActions) {
                    if (remoteDepend.getDependWorkFlowName().equals(dependencyWorkAction.getWorkFlowName()) &&
                            remoteDepend.getRemoteDependName().equals(dependencyWorkAction.getRemoteName()) &&
                            remoteDepend.getRemoteActionName().equals(dependencyWorkAction.getWorkActionName())) {
                        dependencyWorkAction.setRemoteSwitch(true);
                    }
                }
                eventResponse.setDesc("instance is in pending and remove success");
                eventResponse.setStatus(HttpServletResponse.SC_OK);
                return eventResponse;
            }
        }
        try {
            WorkFlow workFlow = workFlowService.getWorkFlowInstance(remoteDepend.getWorkFlowInstanceid());
            if (workFlow != null) {
                workAction = workFlow.getWorkActionByName(remoteDepend.getRemoteDependName());
                List<DependencyWorkAction> dependencyWorkActions = workAction.getDependendWorkActions();
                if (dependencyWorkActions != null && dependencyWorkActions.size() > 0) {
                    for (DependencyWorkAction dependencyWorkAction : dependencyWorkActions) {
                        if (remoteDepend.getRemoteWorkFlowName().equals(dependencyWorkAction.getWorkFlowName()) &&
                                remoteDepend.getRemoteActionName().equals(dependencyWorkAction.getRemoteName()) &&
                                remoteDepend.getRemoteDependName().equals(dependencyWorkAction.getWorkActionName())) {
                            dependencyWorkAction.setRemoteSwitch(true);
                        }
                    }
                }

                int i = 0;

                for (DependencyWorkAction dependencyWorkAction : dependencyWorkActions) {
                    if (dependencyWorkAction.isRemoteSwitch()) {
                        i = i + 1;
                    }
                }
                if (i == dependencyWorkActions.size()) {
                    workAction.statusTransfer(workAction, WorkFlowEvent.SUBMIT);
                }
                eventResponse.setDesc("remote call " + workAction.getActionName() + " has been succeed");
                eventResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
                logger.error("启动远程workFlow实例的id,不对");
                eventResponse.setDesc(remoteDepend.getRemoteDependName() + "has not run");
                eventResponse.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            eventResponse.setDesc("remote call " + remoteDepend.getRemoteDependName() + " has been fail" + e.toString());
        }
        return eventResponse;
    }

    @Override
    public EventResponse doKill(String remoteWorkFlow) {
        EventResponse event = new EventResponse();
        RemoteWorkFlow rwf = JSONObject.parseObject(remoteWorkFlow, RemoteWorkFlow.class);
        WorkFlow workFlow = null;

        if (rwf.getStatus() == WorkRunStatus.RUNNING) {
            workFlow = WorkFlowService.cachedFlowInstance.get(rwf.getRemoteWorkFlowInstanceId());
            if (workFlow != null) {
                for (WorkActionBase actionBase : workFlow.getActions()) {
                    actionBase.doKill();
                }
                workFlow.getEndWorkAction().doKill();
                WorkFlowService.cachedFlowInstance.remove(rwf.getRemoteWorkFlowInstanceId());
            }
        }

        if (rwf.getStatus() == WorkRunStatus.PENDING) {
            workFlow = WorkFlowService.pendingFlowInstance.get(rwf.getRemoteWorkFlowInstanceId());
            if (workFlow != null) {
                WorkFlowService.pendingFlowInstance.remove(rwf.getRemoteWorkFlowInstanceId());
            }
        }

        if (workFlow != null) {
            this.workFlowInstanceMapper.updateWorkFlowStatus(workFlow.getInstanceid(),
                    WorkRunStatus.KILLED,
                    new Timestamp(System.currentTimeMillis()));
        }

        event.setDesc("do kill success ");
        event.setStatus(HttpServletResponse.SC_OK);
        return event;
    }

    @Override
    public EventResponse schedulerResume(String instanceid) {
        EventResponse resp = new EventResponse();
        /**
         * 已经失败了的定时任务实例还未写,先做在RUNNING状态的
         */
        WorkFlow workFlow = WorkFlowService.cachedFlowInstance.get(instanceid);
        if (workFlow == null) {
            resp.setDesc("重启失败");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return resp;
        } else {
            Map<String, WorkActionRelation> relations = workFlow.getRelations();
            for (String actionName : relations.keySet()) {
                WorkActionBase workAction = workFlow.getWorkActionByName(actionName);
                WorkRunStatus status = workAction.getStatus();
                if (status.equals(WorkRunStatus.SUCCESS)) {
                    workAction.setSubmit(false);
                    workAction.setStatus(WorkRunStatus.SUCCESS);
                } else {
                    workAction.setSubmit(true);
                    try {
                        workAction.statusTransfer(workAction, WorkFlowEvent.SUBMIT);
                    } catch (InterruptedException e) {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.setDesc("从内存中重启失败: " + e.getMessage());
                        logger.error("从内存中重启报错 ", e);
                    }
                }
            }
            resp.setDesc("从内存中重启成功");
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        return resp;
    }

    @Override
    public LineResult queryWorkFlowLogger(String instanceid, long size) {
        List<String> content = new ArrayList<>();
        LineResult lineResult = new LineResult();
        try {
            String fileName = wfcp.getLogPath() + instanceid + ".txt";
            File file = new File(fileName);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(fileName));//构造一个BufferedReader类来读取文件
                String s = null;
                int lines = 0;
                //使用readLine方法，一次读一行
                while ((s = br.readLine()) != null) {
                    lines = lines + 1;
                    if (size <= lines) {
                        size = size + 1;
                        content.add(s);
                    }
                }
                br.close();
                lineResult.setContent(content);
                lineResult.setLine(size);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lineResult;
    }

    @Override
    public EventResponse resume(String instanceJson) {
        EventResponse event = new EventResponse();
        WorkFlowInstance workFlowInstance = JSONObject.parseObject(instanceJson, WorkFlowInstance.class);
        String instanceid = workFlowInstance.getInstanceid();
        WorkFlow workFlow = WorkFlowService.cachedFlowInstance.get(instanceid);
        if (workFlow == null) {
            WorkFlowInstance wfi = this.workFlowInstanceMapper.selectWorkInstanceByid(instanceid);
            try {
                workFlow = this.flowActionUtils.decodeWorkFlowInstance(wfi);
                workFlow.initialize();
                workFlow.setWorkDaoService(workDaoService);
                workFlow.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
                workFlow.getEndWorkAction().setWorkFlowService(workFlowService);
                workFlow.buildForWorkFlowInstance();
                this.executorDispatcher.setWorkFlowEndaction(workFlow);
                this.executorDispatcher.setOutputStream(workFlow);
                this.workFlowService.addWorkFlowInstance(workFlow);

            } catch (ParseActionException |
                    CloneNotSupportedException |
                    NoParamException |
                    ParamDataTypeException e) {
                logger.error(" 从数据库中重启报错 ", e);
            }
        }
        List<WorkActionInstance> workActionInstances = this.workActionInstanceMapper.queryActionInstancesForInstanceId(instanceid);
        for (WorkActionInstance workActionInstance : workActionInstances) {
            String actionName = workActionInstance.getActionName();
            if (!(actionName.equals("START"))) {
                WorkRunStatus status = workActionInstance.getStatus();
                WorkActionBase actionBase = workFlow.getWorkActionByName(actionName);
                if (status.equals(WorkRunStatus.SUCCESS)) {
                    actionBase.setSubmit(false);
                    actionBase.setStatus(WorkRunStatus.SUCCESS);
                } else {
                    actionBase.setSubmit(true);
                    try {
                        actionBase.statusTransfer(actionBase, WorkFlowEvent.SUBMIT);
                    } catch (InterruptedException e) {
                        logger.error(" 从内存中重启报错 ", e);
                    }
                }
            }
        }
//        WorkFlow workFlow = null
//        if (workFlowInstance.getStatus() == WorkRunStatus.RUNNING) {
//            workFlow = WorkFlowService.cachedFlowInstance.get(workFlowInstance.getInstanceid());
//            logger.info("in WorkFlowService.cachedFlowInstance");
//        }
//        if (workFlowInstance.getStatus() == WorkRunStatus.PENDING) {
//            workFlow = WorkFlowService.pendingFlowInstance.get(workFlowInstance.getInstanceid());
//        }
//        if (workFlow != null) {
//            List<WorkActionBase> actions = workFlow.getActions();
//            for (WorkActionBase action : actions) {
//                action.doKill();
//                System.out.println(action.getActionName() + " do kill");
//            }
//            workFlow.getEndWorkAction().doResume();
//            this.workFlowInstanceMapper.updateWorkFlowStatus(workFlow.getInstanceid(),
//                    WorkRunStatus.KILLED,
//                    new Timestamp(System.currentTimeMillis()));
//            try {
//                this.workDaoService.resumeStart(workFlow);
//            } catch (CloneNotSupportedException | ParseActionException | UnknownHostException | NoParamException |
//                    ParamDataTypeException | InterruptedException e) {
//                e.printStackTrace();
//                event.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                event.setDesc(e.toString());
//                return event;
//            }
//        } else {
//            this.workFlowInstanceMapper.updateStatus(workFlowInstance.getInstanceid(), WorkRunStatus.DISTRIBUTED);
//        }
        event.setStatus(HttpServletResponse.SC_OK);
        event.setDesc(instanceid + " resume success");
        return event;
    }

    @Override
    public void removePendingStatus(String instanceid) {
        WorkFlowService.pendingFlowInstance.remove(instanceid);
    }

    @Override
    public EventResponse remoteFail(String remoteWorkFlow) {
        EventResponse event = new EventResponse();
        RemoteWorkFlow rwf = JSONObject.parseObject(remoteWorkFlow, RemoteWorkFlow.class);
        WorkFlow workFlow = WorkFlowService.cachedFlowInstance
                .get(rwf.getRemoteWorkFlowInstanceId());
        try {

            if (workFlow != null) {
                if (rwf.isSubmit()) {
                    List<WorkActionBase> actions = workFlow.getActions();
                    for (WorkActionBase action : actions) {
                        action.doFail();
                        action.getWorkFlow().getEndWorkAction().doFail();
                    }
                    event.setStatus(HttpServletResponse.SC_OK);
                    event.setDesc(workFlow.getFlowName() + " is fail ");
                    return event;
                }

                List<WorkActionBase> actions = workFlow.getActions();
                for (WorkActionBase action : actions) {
                    if (action.getActionName().equals(rwf.getDependActionName())) {
                        action.doFail();
                        if (action.getWorkFlow().getEmails() != null && action.getWorkFlow().getEmails().size() > 0) {
                            String sb = new StringBuilder(50).append("服务器: ").append(rwf.getRemoteIp()).append(" 运行实例id: ").append(rwf.getRemoteWorkFlowInstanceId())
                                    .append("it's remote action: ").append(action.getActionName()).append(" is fail").toString();
                            RemoteProcedureException e = new RemoteProcedureException(sb.toString());
                            action.sendEmailByWorkFlow(e);
                        }
                    }
                }
                Map<String, WorkActionRelation> relations = workFlow.getRelations();
                for (String key : relations.keySet()) {
                    WorkActionRelation workActionRelation = relations.get(key);
                    List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
                    List<RemoteWorkAction> remoteDepends = workActionRelation.getRemoteDepends();
                    if (dependWorkActions != null && remoteDepends != null) {
                        for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                            if (dependWorkAction.getWorkFlowName().equals(rwf.getRemoteWorkFlowName())
                                    && dependWorkAction.getWorkActionName().equals(rwf.getRemotActionName())) {

                                for (RemoteWorkAction remoteDepend : remoteDepends) {
                                    String ip = remoteDepend.getIp();
                                    String workFlowInstanceid = remoteDepend.getWorkFlowInstanceid();
                                    String wfName = StringUtils.substringsBetween(workFlowInstanceid, "_", "_")[0];
                                    RemoteWorkFlow remoteWF = new RemoteWorkFlow();
                                    remoteWF.setRemoteIp(ip);
                                    remoteWF.setRemoteWorkFlowInstanceId(workFlowInstanceid);
                                    remoteWF.setRemoteWorkFlowName(wfName);
                                    remoteWF.setRemotActionName(remoteDepend.getRemoteActionName());
                                    ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("remoteFail");
                                    ref.setUrl(ip);
                                    ref.get().remoteFail(JSONObject.toJSONString(remoteWF));
                                }
                            }
                        }
                    }
                }
            } else {
                this.workDaoService.updateWorkFlowInstanceStatus(rwf.getRemoteWorkFlowInstanceId(), WorkRunStatus.FAILED);
            }

            event.setStatus(HttpServletResponse.SC_OK);
            event.setDesc(workFlow.getFlowName() + " is fail ");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            event.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            event.setDesc(" workFlow: " + workFlow.getFlowName() + " do fail is fail");
        }
        return event;
    }

    @Override
    public int queue() {
        return WorkFlowService.cachedFlowInstance.size();
    }

    @Override
    public EventResponse dispatcherScheduler(String workSchedulerJson) {
        WorkScheduler workScheduler = this.flowActionUtils.parseWorkSchedulerJson(workSchedulerJson);
        UserInfo user = this.workDaoService.getUserInfoByID(2);
        String groupName = user.getGroupName();
        EventResponse eventResponse = new EventResponse();
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler;


        try {
            scheduler = schedulerFactory.getScheduler();
            StringBuilder sb = new StringBuilder(50);
            /**
             * 测试为了方便所以加了UUID.randomUUID().toString()
             */
            sb.append(workScheduler.getSchedulerName() + UUID.randomUUID().toString());
            JobDetail job = JobBuilder.newJob(DispathcerSchedulerSubmiter.class)
                    .withIdentity(sb.toString(), groupName).build();

            beanMaps.put("com.banggood.xwork.dao.service.workDaoService", workDaoService);
            beanMaps.put("com.banggood.xwork.scheduler.core.service.workSchedulerService", workSchedulerService);
            System.out.println(" beanMaps.put(workScheduler.getInstanceid(), workScheduler.getVersions());" + workScheduler.getInstanceid());
            job.getJobDataMap().put(workScheduler.getInstanceid(), workScheduler);
            job.getJobDataMap().put("key", workScheduler.getInstanceid());
            logger.info("开始定时任务的instanceid为{} ", workScheduler.getInstanceid());

            WorkSchedulerService.jobKeyMap.put(workScheduler.getInstanceid(), job.getKey());
            WorkSchedulerService.schedulerMap.put(workScheduler.getInstanceid(), scheduler);
            System.out.println(" WorkSchedulerService.schedulerMap.put(workScheduler.getInstanceid(), scheduler);" + workScheduler.getInstanceid());

            CronTrigger trigger = newTrigger().withIdentity(sb.toString(), groupName)
                    .withSchedule(cronSchedule(workScheduler.getCronStr())).build();
            logger.info(" 定时任务启动,cron表达式为: {} ", workScheduler.getCronStr());
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            eventResponse.setStatus(HttpServletResponse.SC_OK);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            eventResponse.setDesc(e.toString());
            eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return eventResponse;
    }

    @Override
    public void schedulerDoKill(String schedulerJson) {
        WorkSchedulerInstance instance = JSONObject.parseObject(schedulerJson, WorkSchedulerInstance.class);
        JobKey jobKey = WorkSchedulerService.jobKeyMap.get(instance.getInstanceid());
        Scheduler scheduler = WorkSchedulerService.schedulerMap.get(instance.getInstanceid());
        if (jobKey != null && scheduler != null) {
            try {
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
            WorkSchedulerService.jobKeyMap.remove(instance.getInstanceid());
            WorkSchedulerService.schedulerMap.remove(instance.getInstanceid());
        }
        this.schedulerInstanceMapper.updateSchedulerStatus(
                instance.getWfInstanceid(),
                WorkRunStatus.KILLED);
    }

}
