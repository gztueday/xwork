package com.banggood.xwork.dao.service;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.banggood.xwork.core.common.*;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.ExecutorDispatcher;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.banggood.xwork.dao.mapper.*;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class WorkDaoServiceImpl implements WorkDaoService {

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private UserInfoMapper userInfoMapper;

    @Autowired
    private FlowActionUtils flowActionUtils;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowInfoMapper workFlowInfoMapper;

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowInstanceMapper workFlowInstanceMapper;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkActionInstanceMapper workActionInstanceMapper;

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private WorkSchedulerService workSchedulerService;

    @Autowired
    private WorkFlowConfigProperty wfcp;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowRelationMapper workFlowRelationMapper;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerInstanceMapper schedulerInstanceMapper;

    @Autowired
    private SeekOut seekOut;

    private final static Logger logger = LoggerFactory.getLogger(WorkDaoServiceImpl.class);

    @Override
    public void updateRelation(Map<String, WorkActionRelation> workActionRelation, String flowName, String fatherName) {
        this.workFlowRelationMapper.updateRelationJson(JSONObject.toJSONString(workActionRelation), flowName, fatherName);
    }

    @Override
    public boolean updateWorkFlow(WorkFlow flow, UserInfo user, WorkFlowConfig config) throws ParseActionException {
        WorkFlowInfo flowInfo = this.flowActionUtils.encodeWorkFlowInfo(flow);
        Timestamp lastUpdateTime = new Timestamp(System.currentTimeMillis());
        this.workFlowInfoMapper.update(
                flow.getFlowName(),
                flowInfo.getConfigParamJSON(),
                flowInfo.getRelationsJson(),
                flowInfo.getActions(),
                user.getUserid(),
                false,
                lastUpdateTime,
                flow.getDependName(),
                flowInfo.getEmailJson(),
                flowInfo.getCreateTime(),
                false,
                flowInfo.getRequestObj(),
                config.getUuid());
        return true;
    }

    @Override
    public boolean addWorkFlow(WorkFlow flow, UserInfo user) throws ParseActionException {

        WorkFlowInfo flowInfo = this.flowActionUtils.encodeWorkFlowInfo(flow);

        Timestamp lastUpdateTime = new Timestamp(System.currentTimeMillis());
        this.workFlowInfoMapper.addWorkActionFlow(
                flow.getFlowName(),
                flowInfo.getConfigParamJSON(),
                flowInfo.getRelationsJson(),
                flowInfo.getActions(),
                user.getUserid(),
                false,
                lastUpdateTime,
                flow.getDependName(),
                flowInfo.getEmailJson(),
                flowInfo.getCreateTime(),
                false,
                flowInfo.getRequestObj(),
                flow.getUpdateUUID(),
                flow.getRunParamJson(),
                flow.getDescription(),
                flow.getSubWorkFlowJson());
        return true;
    }

    @Override
    public String addWorkFlowInstance(WorkFlow workFlow, String instanceId) throws ParseActionException, UnknownHostException {
        WorkFlowInstance instance = this.flowActionUtils.encodeWorkFlowInstance(workFlow);
        this.workFlowInstanceMapper.addFlowInstance(instance.getFlowName(), instance.getStatus(),
                instance.getRunParamJSON(), instance.getConfigParamJSON(), instance.getWorkFlowInfoJSON(),
                instance.getRunTime(), instance.getStartTime(), instance.getSchedulerid(), instance.getSubmiter(),
                instance.getInstanceid(), instance.getExecuteIP(), false, instanceId, 0, JSONObject.toJSONString(workFlow.getArguments()));
        return instance.getInstanceid();
    }

    @Override
    public String addWorkFlowInstanceByScheduler(WorkFlow wf, String instanceId, int version) throws ParseActionException, UnknownHostException {
        WorkFlowInstance instance = this.flowActionUtils.encodeWorkFlowInstance(wf);
        this.workFlowInstanceMapper.addFlowInstance(instance.getFlowName(), instance.getStatus(),
                instance.getRunParamJSON(), instance.getConfigParamJSON(), instance.getWorkFlowInfoJSON(),
                instance.getRunTime(), instance.getStartTime(), instance.getSchedulerid(), instance.getSubmiter(),
                instance.getInstanceid(), instance.getExecuteIP(), false, instanceId, version, null);
        return instance.getInstanceid();
    }

    @Override
    public String addWorkFlowInstanceByRemote(WorkFlow wf)
            throws ParseActionException, UnknownHostException {
        WorkFlowInstance instance = this.flowActionUtils.encodeWorkFlowInstanceByRemote(wf);
        this.workFlowInstanceMapper.addFlowInstance(instance.getFlowName(), instance.getStatus(),
                instance.getRunParamJSON(), instance.getConfigParamJSON(), instance.getWorkFlowInfoJSON(),
                instance.getRunTime(), instance.getStartTime(), instance.getSchedulerid(), instance.getSubmiter(),
                instance.getInstanceid(), instance.getExecuteIP(), false, null, 0, null);
        return wf.getInstanceid();
    }

    @Override
    public boolean updateWorkFlowInstanceStatus(WorkFlow workFlow) throws ParseActionException, UnknownHostException {

        WorkFlowInstance instance = this.flowActionUtils.encodeWorkFlowInstance(workFlow);
        this.workFlowInstanceMapper.updateRunParamStatus(instance.getRunParamJSON(), instance.getInstanceid(),
                instance.getStatus(), instance.getEndTime());
        return false;
    }

//    @Override
//    public boolean updateWorkFlowInstanceStatus(WorkFlowInstance instance) throws ParseActionException, UnknownHostException {
//
//        this.workFlowInstanceMapper.updateWorkFlowInstanceStatus(instance.getStatus(), instance.getVersions(), instance.getSchedulerid(), instance.getFatherInstanceId(), new Date(), new Date());
//        return false;
//    }

    @Override
    public String getLocalIp() {
        return this.serviceRegistry.getDubbopIP();
    }

    /**
     * workFlowInstance  fail状态下的action级别的重跑
     *
     * @param instanceId
     * @return
     * @throws ParseActionException
     * @throws CloneNotSupportedException
     * @throws InterruptedException
     * @throws UnknownHostException
     * @throws NoParamException
     * @throws ParamDataTypeException
     */
    @Override
    public String resume(String instanceId) throws ParseActionException, CloneNotSupportedException, InterruptedException, UnknownHostException, NoParamException, ParamDataTypeException {
        WorkFlowInstance instance = this.workFlowInstanceMapper.selectWorkInstanceByid(instanceId);
        if (instance.getStatus().equals(WorkRunStatus.RUNNING)) {
            return instance.getInstanceid() + " 这个实例正在运行,请不要重启";
        } else if (instance.getStatus().equals(WorkRunStatus.PENDING)) {
            return instance.getInstanceid() + " 这个实例为等待状态,无法重启";
        } else {
            ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("resume");
            ref.setUrl(instance.getExecuteIP());
            EventResponse eventResponse = ref.get().resume(JSONObject.toJSONString(instance));
            if (eventResponse.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                logger.error(eventResponse.getDesc());
            }
            return eventResponse.getDesc();
        }
    }

//    @Override
//    public void resumeStart(WorkFlow work) throws NoParamException, ParseActionException, ParamDataTypeException, InterruptedException, CloneNotSupportedException, UnknownHostException {
//        List<WorkActionBase> actions = work.getActions();
//        List<WorkActionBase> list = new ArrayList<>();
//        list.addAll(actions);
//        list.add(work.getStartWorkAction());
//        list.add(work.getEndWorkAction());
//        for (WorkActionBase action : list) {
//            action.setSubmit(true);
//            action.setStatus(WorkRunStatus.DISTRIBUTED);
//            String number = StringUtils.substringAfterLast(action.getInstanceid(), "_");
//            String instanceId = StringUtils.substringBeforeLast(action.getInstanceid(), "_");
//            int integer = Integer.parseInt(number) + 1;
//            action.setInstanceid(instanceId + integer);
//        }
//        work.getRunningActions().clear();
//        this.workFlowInstanceMapper.updateWorkFlowStatus(work.getInstanceid(),
//                WorkRunStatus.RUNNING,
//                new Timestamp(System.currentTimeMillis()));
//        work.getStartWorkAction().statusTransfer(work.getStartWorkAction(), WorkFlowEvent.SUBMIT);
//    }

    @Override
    public String doKill(String instanceId) {
        WorkFlowInstance instance = this.workFlowInstanceMapper.selectWorkInstanceByid(instanceId);
        if (!(instance.getStatus().equals(WorkRunStatus.SUCCESS) ||
                instance.getStatus().equals(WorkRunStatus.FAILED))) {

            if (instance.getStatus().equals(WorkRunStatus.RUNNING) ||
                    instance.getStatus().equals(WorkRunStatus.PENDING)) {

                ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("doKill");
                ref.setUrl(instance.getExecuteIP());
                RemoteWorkFlow remoteWorkFlow = new RemoteWorkFlow();
                remoteWorkFlow.setStatus(instance.getStatus());
                remoteWorkFlow.setRemoteWorkFlowInstanceId(instance.getInstanceid());

                EventResponse eventResponse = ref.get().doKill(JSONObject.toJSONString(remoteWorkFlow));

                if (eventResponse.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                    logger.error(eventResponse.getDesc());
                }
                return eventResponse.getDesc();
            }

            if (instance.getStatus().equals(WorkRunStatus.DISTRIBUTED)) {
                this.workFlowInstanceMapper.updateWorkFlowStatus(instance.getInstanceid(),
                        WorkRunStatus.KILLED,
                        new Timestamp(System.currentTimeMillis()));
                return instanceId + " is distributed , and to be kill";
            }

        } else {
            return instanceId + " is success or failed , so can not be killed ";
        }
        return instanceId;
    }

    @Override
    public void updateWorkFlowInstance(WorkFlowInstance instance, RemoteWorkAction depend) {
        String workFlowInfoJSON = instance.getWorkFlowInfoJSON();
        WorkFlowInfo workFlowInfo = JSONObject.parseObject(workFlowInfoJSON, WorkFlowInfo.class);
        String relationsJson = workFlowInfo.getRelationsJson();
        HashMap<String, WorkActionRelation> relationMap = JSONObject.parseObject(relationsJson, new TypeReference<HashMap<String, WorkActionRelation>>() {
        });
        if (relationMap != null && relationMap.size() > 0) {
            WorkActionRelation workActionRelation = relationMap.get(depend.getWorkActionName());
            List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
            if (dependWorkActions != null && dependWorkActions.size() > 0) {
                for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                    if (dependWorkAction.getWorkFlowName().equals(depend.getRemoteWorkFlowName()) &&
                            dependWorkAction.getWorkActionName().equals(depend.getRemoteActionName())) {
                        dependWorkAction.setRemoteSwitch(true);
                    }
                }
            }
        }
        workFlowInfo.setRelationsJson(JSONObject.toJSONString(relationMap));
        instance.setWorkFlowInfoJSON(JSONObject.toJSONString(workFlowInfo));
        this.workFlowInstanceMapper.updateWorkFlowInstance(instance);
    }

    @Override
    public WorkFlowInstance selectWFByFatherId(String fatherInstanceId, String workFlowName) {
        return this.workFlowInstanceMapper.selectWorkInstance(fatherInstanceId, workFlowName);
    }

    @Override
    public UserInfo getUserInfoByName(String userName) {
        return this.userInfoMapper.getUserInfoByName(userName);
    }

    @Override
    public void updateActionStatus(WorkActionBase action) {

        if (this.workActionInstanceMapper.checkActionInstance(action.getInstanceid())) {
            WorkActionInstance instance = this.flowActionUtils.encodeWorkActionInstance(action, action.getWorkFlow());
            try {
                this.workActionInstanceMapper.updateStatus(instance.getInstanceid(), instance.getStatus(), instance.getEndTime(), instance.getRunTime(), instance.getStartTime(), instance.getOutPutJSON());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            this.addActionInstance(action);
        }
    }

    @Override
    public void addActionInstance(WorkActionBase action) {
        WorkActionInstance instance = this.flowActionUtils.encodeWorkActionInstance(action, action.getWorkFlow());
        this.workActionInstanceMapper.addWorkActionInstance(instance.getFlowName(), instance.getActionName(),
                instance.getSchedulerid(), instance.getOutPutJSON(), instance.getRunParamJSON(),
                instance.getConfigParamJSON(), instance.getStatus(), instance.getRunTime(), instance.getStartTime(),
                instance.getEndTime(), instance.isConditionPass(), instance.getClazz(), instance.getInstanceid(),
                instance.getFlowInstanceid());

    }

    @Override
    public UserInfo getUserInfoByID(long userid) {
        return this.userInfoMapper.getUserInfoByID(userid);
    }

    /**
     * 把workFlow的PENDING转台转为RUNNING状态
     *
     * @param workFlow
     * @param status
     * @return
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    @Override
    public int lockWorkFlowInstanceForPending(WorkFlow workFlow, WorkRunStatus status) throws ParseActionException, UnknownHostException {
        return this.workFlowInstanceMapper.lockFlowInstanceForPending(workFlow.getInstanceid(), WorkRunStatus.RUNNING, status, ExecutorDispatcher.ip);
    }

    @Override
    public void checkDeleteRelation(String fatherName) throws ParseActionException, CloneNotSupportedException {
        List<WorkFlowInfo> workFlowInfos = this.workFlowRelationMapper.selectByRelation(fatherName);
        for (WorkFlowInfo workFlowInfo : workFlowInfos) {
            boolean open = true;
            WorkFlow workFlow = flowActionUtils.decodeWorkFlowInfo(workFlowInfo);
            Map<String, WorkActionRelation> relations = workFlow.getRelations();
            for (String key : relations.keySet()) {
                WorkActionRelation workActionRelation = relations.get(key);

                if (workActionRelation.getRemoteDepends() != null &&
                        workActionRelation.getRemoteDepends().size() > 0) {
                    open = false;
                }

                if (workActionRelation.getRemoteDepends() != null &&
                        workActionRelation.getRemoteDepends().size() > 0) {
                    open = false;
                }

            }
            if (open) {
                this.workFlowRelationMapper.deleteByRelation(workFlowInfo.getFlowRelation(), workFlow.getFlowName());
            }
        }
    }

    @Override
    public WorkFlow getWorkFlowByRelation(String flowName, String fatherName) throws ParseActionException, CloneNotSupportedException {
        WorkFlowInfo workFlowInfo = this.workFlowRelationMapper.getWorkFlowByName(flowName, fatherName);
        if (workFlowInfo == null) {
            return null;
        }
        WorkFlow workFlow = flowActionUtils.decodeWorkFlowInfo(workFlowInfo);
        workFlow.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
        workFlow.setFlowName(flowName);
        workFlow.setWorkDaoService(this);
        workFlow.setFlowRelation(workFlowInfo.getFlowRelation());
        return workFlow;
    }

    @Override
    public void insertRelation(WorkFlow flow, String fatherName, WorkFlowConfig config) throws ParseActionException {
        WorkFlowInfo flowInfo = this.flowActionUtils.encodeWorkFlowInfo(flow);

        Timestamp lastUpdateTime = new Timestamp(System.currentTimeMillis());
        this.workFlowRelationMapper.insertRelation(flow.getFlowName(),
                flowInfo.getConfigParamJSON(),
                flowInfo.getRelationsJson(),
                flowInfo.getActions(),
                flow.getUpdaterid(),
                false,
                lastUpdateTime,
                flow.getDependName(),
                flowInfo.getEmailJson(),
                flowInfo.getCreateTime(),
                false,
                flowInfo.getRequestObj(),
                config.getUuid(),
                fatherName);
    }

    @Override
    public WorkFlow getWorkFlow(String flowName) throws ParseActionException, CloneNotSupportedException {

        WorkFlowInfo workFlowInfo = this.workFlowInfoMapper.getWorkActionFlowByName(flowName);
        if (workFlowInfo == null) {
            throw new ParseActionException("can not find work flow config for name:" + flowName);
        }
        WorkFlow workFlow = flowActionUtils.decodeWorkFlowInfo(workFlowInfo);
        workFlow.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
        workFlow.setFlowName(flowName);
        workFlow.setWorkDaoService(this);
        return workFlow;
    }

    @Override
    public List<WorkFlow> getFlowInstancesByStatus(WorkRunStatus status) throws ParseActionException, CloneNotSupportedException {
        List<WorkFlowInstance> instances = this.workFlowInstanceMapper.getFlowInstancesByStatus(status);
        List<WorkFlow> flows = new ArrayList<>(instances.size());
        for (WorkFlowInstance instance : instances) {
            flows.add(this.flowActionUtils.decodeWorkFlowInstance(instance));
        }
        return flows;
    }

    @Override
    public WorkFlow lockWorkFlowInstance(WorkFlow workFlow, WorkRunStatus status)
            throws ParseActionException, UnknownHostException {
        WorkFlowInstance instance = this.flowActionUtils.encodeWorkFlowInstance(workFlow);
        int count = this.workFlowInstanceMapper.lockFlowInstance(instance.getInstanceid(), WorkRunStatus.PENDING, status, instance.getExecuteIP());
        if (count > 0) {
            workFlow.setStatus(WorkRunStatus.PENDING);
            workFlow.setInstanceid(instance.getInstanceid());
            return workFlow;
        }
        return null;
    }

    @Override
    public List<SchedulerRelationInfo> selectByRelationId(String coordinatorName) {
        return null;
    }

    @Override
    public WorkFlow getWorkFlowForUUID(String uuid) throws ParseActionException, CloneNotSupportedException {
        WorkFlowInfo workFlowInfo = this.workFlowInfoMapper.getWorkFlowForUUID(uuid);
        WorkFlow workFlow = flowActionUtils.decodeWorkFlowInfo(workFlowInfo);
        workFlow.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
        workFlow.setFlowName(workFlowInfo.getFlowName());
        workFlow.setWorkDaoService(this);
        return workFlow;
    }

    @Override
    public void updateWorkFlowInstanceToFail(WorkFlow workFlow, WorkRunStatus failed) {

        this.workFlowInstanceMapper.updateWorkFlowInstanceToFail(workFlow.getInstanceid(), failed);

    }

//    @Override
//    public WorkFlow loadSchedulerRelations(WorkFlow workFlow, String relationid) {
//        return null;
//    }
//
//    @Override
//    public String selectInstance(String fatherInstanceId, String workFlowName) {
//        return this.workFlowInstanceMapper.selectInstance(fatherInstanceId, workFlowName);
//    }

    @Override
    public void updateWorkFlowInstanceStatus(String remoteWorkFlowInstanceId, WorkRunStatus failed) {
        this.workFlowInstanceMapper.updateWorkFlowStatus(remoteWorkFlowInstanceId, failed, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public void updateSchedulerInstanceStatus(WorkFlow workFlow, WorkRunStatus status) {
        this.schedulerInstanceMapper.updateSchedulerInstanceToFail(workFlow.getSchedulerid(), workFlow.getInstanceid(), status, new Date());
    }

    @Override
    public String queryWorkFlowInstanceIp(String wfInstanceid) {
        return this.workFlowInstanceMapper.queryWorkFlowInstanceIp(wfInstanceid);
    }

    @Override
    public WorkSchedulerInstance querySchedulerInstanceByWorkFlowInstanceid(String instanceid) {
        return this.schedulerInstanceMapper.queryByWorkFlowInstanceId(instanceid);
    }

    @Override
    public long queryRightVersion(long leftVersion, long calculate) {
        return seekOut.calculatorDependVersion(leftVersion, calculate);
    }

    @Override
    public WorkSchedulerInstance querySchedulerInstanceByScheduleridAndVersion(String rightSchedulerInstance, long rightVersion) {
        return this.schedulerInstanceMapper.querySchedulerByInstanceidAndVersion(rightSchedulerInstance, rightVersion);
    }

    @Override
    public void updateSchedulerRelation(String instanceid, long rightVersion, String jsonString) {
        this.workSchedulerService.updateRelations(instanceid, rightVersion, jsonString);
    }

    @Override
    public WorkActionInstance queryActionByWFidAndActionName(String wfInstanceid, String remoteName) {
        return this.workActionInstanceMapper.queryActionByWFidAndActionName(wfInstanceid, remoteName);
    }

}
