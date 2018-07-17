package com.bigdata.xwork.dao.service;

import com.bigdata.xwork.action.core.WorkActionBase;
import com.bigdata.xwork.action.core.WorkActionRelation;
import com.bigdata.xwork.action.core.WorkFlow;
import com.bigdata.xwork.action.core.WorkRunStatus;
import com.bigdata.xwork.core.common.RemoteWorkAction;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.entity.*;
import com.bigdata.xwork.dao.entity.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public interface WorkDaoService {

	/*-----------------------------以下是关于工作流节点的待久化操作--------------------------------------*/

    /**
     * 更新动作节点状态
     */
    void updateActionStatus(WorkActionBase action);

    /**
     * 添加运行实例
     */
    void addActionInstance(WorkActionBase action);

	/*-----------------------------以下是关于工作流配置的待久化操作--------------------------------------*/

    /**
     * 获取work flow配置信息
     *
     * @return
     * @throws ParseActionException
     * @throws CloneNotSupportedException
     */
    WorkFlow getWorkFlow(String flowName) throws ParseActionException, CloneNotSupportedException;

    /**
     * 添加工作流配置信息
     *
     * @throws ParseActionException
     */
    boolean addWorkFlow(WorkFlow flow, UserInfo user) throws ParseActionException;

	/*-----------------------------以下是关于工作流运行实例的待久化操作--------------------------------------*/

    /**
     * 添加work flow运行实例
     *
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    String addWorkFlowInstance(WorkFlow workFlow, String instanceId) throws ParseActionException, UnknownHostException;

    /**
     * 通过schedulerInstance添加workFlowInstance实例
     *
     * @param wf
     * @param instanceId
     * @param version
     * @return
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    String addWorkFlowInstanceByScheduler(WorkFlow wf, String instanceId, int version) throws ParseActionException, UnknownHostException;

    /**
     * 根据状态获取实例
     *
     * @throws CloneNotSupportedException
     * @throws ParseActionException
     */
    List<WorkFlow> getFlowInstancesByStatus(WorkRunStatus status) throws ParseActionException, CloneNotSupportedException;

    /**
     * 更新work flow运行实例状态
     *
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    boolean updateWorkFlowInstanceStatus(WorkFlow workFlow) throws ParseActionException, UnknownHostException;

//    boolean updateWorkFlowInstanceStatus(WorkFlowInstance workFlowInstance) throws ParseActionException, UnknownHostException;

    WorkFlow lockWorkFlowInstance(WorkFlow workFlow, WorkRunStatus status) throws ParseActionException, UnknownHostException;

    List<SchedulerRelationInfo> selectByRelationId(String coordinatorName);

    /*-----------------------------以下是关于调度实例的待久化操作--------------------------------------*/

    /**
     * 获取用户信息
     */
    UserInfo getUserInfoByName(String userName);

    UserInfo getUserInfoByID(long userid);

    int lockWorkFlowInstanceForPending(WorkFlow workFlow, WorkRunStatus pending) throws ParseActionException, UnknownHostException;

    void updateWorkFlowInstanceToFail(WorkFlow workFlow, WorkRunStatus failed);

    String addWorkFlowInstanceByRemote(WorkFlow wf) throws ParseActionException, UnknownHostException;

    String getLocalIp();

    String doKill(String instanceId);

    void updateWorkFlowInstance(WorkFlowInstance instance, RemoteWorkAction depend);

    WorkFlowInstance selectWFByFatherId(String fatherInstanceId, String workFlowName);

    void updateRelation(Map<String, WorkActionRelation> workActionRelation, String flowName, String fatherName);

    boolean updateWorkFlow(WorkFlow flow, UserInfo user, WorkFlowConfig config) throws ParseActionException;

    WorkFlow getWorkFlowForUUID(String flowName) throws ParseActionException, CloneNotSupportedException;

    String resume(String instanceId) throws ParseActionException, CloneNotSupportedException, InterruptedException, UnknownHostException, NoParamException, ParamDataTypeException;

//    void resumeStart(WorkFlow workFlow) throws NoParamException, ParseActionException, ParamDataTypeException, InterruptedException, CloneNotSupportedException, UnknownHostException;

    WorkFlow getWorkFlowByRelation(String flowName, String fatherName) throws ParseActionException, CloneNotSupportedException;

    void insertRelation(WorkFlow workFlow, String fatherName, WorkFlowConfig config) throws ParseActionException;

    void checkDeleteRelation(String flowName) throws ParseActionException, CloneNotSupportedException;

//    WorkFlowInstance selectWFByScheduler(String fatherInstanceId, String workFlowName, int version);

//    WorkFlow loadSchedulerRelations(WorkFlow workFlow, String relationid);

//    void updateRelation(WorkFlowInstance instance, WorkFlow workFlow) throws ParseActionException, UnknownHostException;

//    String selectInstance(String fatherInstanceId, String workFlowName);

    void updateWorkFlowInstanceStatus(String remoteWorkFlowInstanceId, WorkRunStatus failed);

    void updateSchedulerInstanceStatus(WorkFlow workFlow, WorkRunStatus status);

    String queryWorkFlowInstanceIp(String wfInstanceid);

    WorkSchedulerInstance querySchedulerInstanceByWorkFlowInstanceid(String instanceid);

    long queryRightVersion(long leftVersion, long calculate);

    WorkSchedulerInstance querySchedulerInstanceByScheduleridAndVersion(String rightSchedulerInstance, long rightVersion);

    void updateSchedulerRelation(String instanceid, long rightVersion, String jsonString);

    WorkActionInstance queryActionByWFidAndActionName(String wfInstanceid, String remoteName);
}
