package com.banggood.xwork.action.core;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.WorkActionParam.ParamDataType;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.banggood.xwork.core.common.AbstractEnviroment;
import com.banggood.xwork.core.common.DependencyWorkAction;
import com.banggood.xwork.core.common.RemoteWorkAction;
import com.banggood.xwork.core.common.WorkConfig;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.query.AcceptSchedulerObject;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class WorkFlow extends AbstractEnviroment implements Cloneable {

    /**
     * 文件输出流
     */
    private FileOutputStream outputStream = null;

    private List<String> emails = new ArrayList<>();

    private String dependName;
    /**
     * 运行时间
     */
    private Long runTime;

    private Long startTime;

    private Long endTime;

    private String schedulerid;

    private int tyrTime;

    private String instanceid;

    private String uuid;
    /**
     * 配置上面的uuid,编辑时候用的
     */
    private String updateUUID;

    /**
     * 更新的用户
     */
    private long updaterid;

//  private String schedulerInstance;

    private WorkSchedulerService workSchedulerService;

    /**
     * 提交的用户
     */
    private long submiter;

    private WorkRunStatus status;

    private WorkDaoService workDaoService;
    private static int count = 0;
    // 结束action
    public EndWorkAction endWorkAction = null;

    // 开始action
    public StartWorkAction startWorkAction = null;

    private String ip;

    /*---------------------------------配置类型变量----------------------------------------------*/

    private String flowName;
    /**
     * 一个开始加载的fatherWorkFlowInstance实例的id
     */
    private String fatherInstanceId;

    /**
     * 配置的用户
     */
    private long configerid;
    /**
     * work_flow_relation表里面的关系列
     */
    private String flowRelation;

    private List<RemoteWorkFlow> remoteWorkFlows = new ArrayList<>();

    // 节点,不存start 和end节点
    private List<WorkActionBase> actions = new ArrayList<>();

    // 节点关系
    public Map<String, WorkActionRelation> relations = new HashMap<>();
    /**
     * 运行中的action
     */
    private Map<String, WorkActionBase> runningActions = new HashMap<>();

    //-----------------------------------------------------------------------------------------
    private int version = 0;

    /**
     * 前端回显需要保存的数据
     */
    private String requestObj;

    private WorkSchedulerInstance schedulerInstance;

    private String description;

    private List<SubWorkFlow> subWorkFlows;

    private String subWorkFlowJson;

    public String getSubWorkFlowJson() {
        return subWorkFlowJson;
    }

    public void setSubWorkFlowJson(String subWorkFlowJson) {
        this.subWorkFlowJson = subWorkFlowJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private static Logger logger = LoggerFactory.getLogger(WorkFlow.class);
    public final Lock lock;

    private String runParamJson;

    public WorkFlow() {
        this.lock = new ReentrantLock();
        this.registerParam("runTime", new WorkActionParam(true, ParamDataType.STRING));

    }

    public List<SubWorkFlow> getSubWorkFlows() {
        return subWorkFlows;
    }

    public void setSubWorkFlows(List<SubWorkFlow> subWorkFlows) {
        this.subWorkFlows = subWorkFlows;
    }

    public WorkSchedulerInstance getSchedulerInstance() {
        return schedulerInstance;
    }

    public void setSchedulerInstance(WorkSchedulerInstance schedulerInstance) {
        this.schedulerInstance = schedulerInstance;
    }

    public String getFlowRelation() {
        return flowRelation;
    }

    public void setFlowRelation(String flowRelation) {
        this.flowRelation = flowRelation;
    }

    public String getFatherInstanceId() {
        return fatherInstanceId;
    }

    public void setFatherInstanceId(String fatherInstanceId) {
        this.fatherInstanceId = fatherInstanceId;
    }

    public String getRequestObj() {
        return requestObj;
    }

    public void setRequestObj(String requestObj) {
        this.requestObj = requestObj;
    }

    public Map<String, WorkActionBase> getRunningActions() {
        return runningActions;
    }

    public void setRunningActions(Map<String, WorkActionBase> runningActions) {
        this.runningActions = runningActions;
    }

    public FileOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(FileOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public List<DependencyWorkAction> getDependcyWorkActions() {
        List<DependencyWorkAction> dws = new ArrayList<>();
        for (WorkActionBase action : actions) {
            if (action != null && action.getDependendWorkActions() != null) {
                dws.addAll(action.getDependendWorkActions());
            }
        }
        return dws;
    }

    public DependencyWorkAction getDependcyWorkActionByName(String dependcyWorkActionName) {
        List<DependencyWorkAction> dependcyWorkActions = getDependcyWorkActions();
        if (dependcyWorkActions.size() > 0) {
            for (DependencyWorkAction dependencyWorkAction : dependcyWorkActions) {
                if (dependcyWorkActionName.equals(dependencyWorkAction.getRemoteName())) {
                    return dependencyWorkAction;
                }
            }
        }
        return null;
    }

    public WorkActionBase getWorkActionByName(String actionName) {
        for (WorkActionBase action : actions) {
            if (action.getActionName().equals(actionName)) {
                return action;
            }
        }
        logger.error("找不到该action: {}", actionName);
        return null;
    }

    /**
     * 验证完后把相关属性设置进去
     *
     * @param workActionFlowName
     * @param count
     * @throws NoParamException
     * @throws ParamDataTypeException
     */
    private void initialize(String workActionFlowName, CountDownLatch count)
            throws NoParamException, ParamDataTypeException {


        this.uuid = java.util.UUID.randomUUID().toString();
        this.flowName = workActionFlowName;

        this.doCheckConfig();
        WorkConfig runParam = this.getRunParam();
        this.decodeParam(runParam);
        this.runTime = runParam.getLong("runTime");


        this.endWorkAction = new EndWorkAction();
        this.endWorkAction.setUuid(uuid);
        this.startWorkAction = new StartWorkAction();
        this.startWorkAction.setUuid(uuid);
        this.endWorkAction.setWorkFlow(this);
        this.startWorkAction.setWorkFlow(this);
        this.endWorkAction.setWorkDaoService(workDaoService);
        this.startWorkAction.setWorkDaoService(workDaoService);
        logger.info(this.toString());
    }

    private void reConstruct() throws ParseActionException, NoParamException, ParamDataTypeException {

        List<WorkActionBase> tmp = new ArrayList<>();
        tmp.addAll(this.actions);
        tmp.add(endWorkAction);
        tmp.add(startWorkAction);
        this.parseRelation(this.relations, tmp);

    }

    /**
     * 添加action作为叶子结点
     */
    public WorkActionBase appendLeaf(String fatherName, WorkActionBase child) {


        if (this.getWorkActionBase(actions, child.getActionName()) == null) {
            this.actions.add(child);
        }
        child.setWorkFlow(this);
        loopAppend(this.startWorkAction, fatherName, child);
        return child;
    }

    /**
     * 添加子作业任务
     */
    public WorkActionBase appendSubWorkAction(String fatherName, WorkFlow subWorkFlow) {

        this.actions.addAll(subWorkFlow.getActions());

        loopAppend(this.startWorkAction, fatherName, subWorkFlow.takeStartAction());

        this.endWorkAction.setFatherActions(new WorkActionBase[]{subWorkFlow.takeEndAction()});

        subWorkFlow.takeEndAction().setChildrenActions(new WorkActionBase[]{this.endWorkAction});

        return subWorkFlow.takeStartAction();
    }

    /**
     * parse节点关系，从存储中拿到信息后调用
     */
    public void parseRelation(Map<String, WorkActionRelation> relationsMap, List<WorkActionBase> actions)
            throws ParseActionException {
        Iterator<Entry<String, WorkActionRelation>> iter = relationsMap.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, WorkActionRelation> entry = iter.next();
            String actionName = entry.getKey();
            WorkActionRelation relation = entry.getValue();

            WorkActionBase current = getWorkActionBase(actions, actionName);

            if (current == null) {
                throw new ParseActionException("can not find action for name:" + actionName);
            }
            current.setWorkFlow(this);
            List<String> fathers = relation.getFathers();
            List<String> chilren = relation.getChildren();
            List<WorkActionBase> fatherActions = new ArrayList<>(fathers.size());
            List<WorkActionBase> childActions = new ArrayList<>(fathers.size());

            for (String father : fathers) {
                WorkActionBase tmp = getWorkActionBase(actions, father);
                if (tmp == null) {
                    logger.warn("can not find action by the name:" + father);
                } else {
                    fatherActions.add(tmp);
                }

            }

            for (String child : chilren) {
                WorkActionBase tmp = getWorkActionBase(actions, child);
                if (tmp == null) {
                    logger.warn("can not find action by the name:" + child);
                } else {
                    childActions.add(tmp);
                }

            }

            current.setChildrenActions(childActions);
            current.setFatherActions(fatherActions);

        }
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("wf_");
        sb.append(this.flowName);
        sb.append("_");
        sb.append(this.uuid);
        sb.append("_");
        sb.append(this.runTime);

        return sb.toString();

    }

    private WorkActionBase getWorkActionBase(List<WorkActionBase> actions, String actionName) {

        for (WorkActionBase action : actions) {
            if (action != null && action.getActionName().equals(actionName)) {
                return action;
            }
        }
        return null;
    }

    private boolean loopAppend(WorkActionBase startWorkAction, String actionName, WorkActionBase appendAction) {
        boolean find = false;
        for (WorkActionBase action : startWorkAction.listChildActions()) {
            String name = action.getActionName();
            if (actionName.equals(name)) {

                action.setChildrenActions(new WorkActionBase[]{appendAction});
                appendAction.setFatherActions(new WorkActionBase[]{action});

                if (this.relations.containsKey(action.getActionName())) {
                    WorkActionRelation crelation = this.relations.get(action.getActionName());
                    crelation.setActionName(action.getActionName());
                    crelation.addChildName(appendAction.getActionName());
                } else {
                    WorkActionRelation crelation = new WorkActionRelation();
                    crelation.addChildName(appendAction.getActionName());
                    this.relations.put(action.getActionName(), crelation);
                }

                if (this.relations.containsKey(appendAction.getActionName())) {
                    WorkActionRelation frelation = this.relations.get(appendAction.getActionName());
                    frelation.addFatherName(action.getActionName());
                } else {
                    WorkActionRelation frelation = new WorkActionRelation();
                    frelation.setActionName(appendAction.getActionName());
                    frelation.addFatherName(action.getActionName());
                    this.relations.put(appendAction.getActionName(), frelation);
                }
                return true;
            } else {
                if (!action.listChildActions().isEmpty()) {
                    find = loopAppend(action, actionName, appendAction);
                }
            }
        }

        if (!find) {
            if (startWorkAction.getActionName().equals(appendAction.getActionName())) {
                return false;
            }
            if (startWorkAction.getActionName().equals(actionName)) {
                startWorkAction.setChildrenActions(new WorkActionBase[]{appendAction});
                appendAction.setFatherActions(new WorkActionBase[]{startWorkAction});

                return true;
            }

        }

        return find;
    }

    /**
     * 从根节点遍历，然后最后将endWorkAction添加到树的最后
     */
    private void loopToLeaf(WorkActionBase startWorkAction) {
        List<WorkActionBase> children = startWorkAction.listChildActions();

        if (children.size() > 0) {

            if (startWorkAction instanceof StartWorkAction) {
                WorkActionRelation value = new WorkActionRelation();

                for (WorkActionBase action : children) {
                    value.addChildName(action.getActionName());
                }

                this.relations.put(startWorkAction.getActionName(), value);
            }

            for (WorkActionBase child : children) {
                if (!child.getActionName().equals(WorkActionConstant.ENDACTION)) {
                    loopToLeaf(child);
                }

            }
        } else {
            if (startWorkAction instanceof StartWorkAction) {
                throw new RuntimeException("work flow only has start and end action");
            }
            startWorkAction.setChildrenActions(new WorkActionBase[]{this.endWorkAction});
            this.endWorkAction.setFatherActions(new WorkActionBase[]{startWorkAction});

            if (this.relations.containsKey(startWorkAction.getActionName())) {
                WorkActionRelation value = this.relations.get(startWorkAction.getActionName());
                value.addChildName(this.endWorkAction.getActionName());
            } else {
                logger.warn("action {} 没有上一级的actions", startWorkAction.getActionName());
            }

            if (this.relations.containsKey(this.endWorkAction.getActionName())) {
                WorkActionRelation value = this.relations.get(this.getEndWorkAction().getActionName());
                List<String> fathersName = value.getFathers();
                fathersName.add(startWorkAction.getActionName());
            } else {
                WorkActionRelation value = new WorkActionRelation();

                value.addFatherName(startWorkAction.getActionName());
                this.relations.put(this.endWorkAction.getActionName(), value);

            }

        }
    }

    public void buildForWorkFlowInstance()
            throws NoParamException, ParamDataTypeException, ParseActionException, CloneNotSupportedException {
        if (!checkBuild()) {
            this.reConstruct();
        } else {
            loopToLeaf(this.startWorkAction);
        }

        appendSubWorkFlow();

		/* 检查配置是否符合要求,并转换参数 */
        checkAndConvert();
    }

    private void appendSubWorkFlow() throws ParseActionException, CloneNotSupportedException {
        List<SubWorkFlow> subWorkFlows = this.getSubWorkFlows();
        if (subWorkFlows != null && subWorkFlows.size() > 0) {
            for (SubWorkFlow subWorkFlow : subWorkFlows) {
                WorkFlow workFlow = this.workDaoService.getWorkFlow(subWorkFlow.getSubFlowName());
                String childrenName = subWorkFlow.getChildrenAction();
                String fatherName = subWorkFlow.getFatherAction();
                this.getActions().addAll(workFlow.getActions());

                this.appendActionRelation(workFlow, childrenName, fatherName);

            }
        }
    }


    private void appendActionRelation(WorkFlow workFlow, String childrenName, String fatherName) {
        WorkActionBase childrenAction = this.getWorkActionByName(childrenName);
        childrenAction.cleanChildren(childrenName);
        childrenAction.getChildren().add(workFlow.startWorkAction);

        WorkActionBase fatherAction = this.getWorkActionByName(fatherName);
        fatherAction.cleanFather(fatherName);
        fatherAction.getFathers().add(workFlow.endWorkAction);
    }

    private void checkAndConvert() throws NoParamException, ParamDataTypeException {
        for (WorkActionBase action : this.actions) {
            action.doCheckConfig();

            action.setRunTime(runTime);
            action.setUuid(uuid);

            action.decodeParam(this.getRunParam());
        }
    }

    /**
     * 这个方法会设置instanceId
     *
     * @throws NoParamException
     * @throws ParamDataTypeException
     * @throws ParseActionException
     */
    public void build() throws NoParamException, ParamDataTypeException, ParseActionException {


        if (!checkBuild()) {
            this.reConstruct();
        } else {
            loopToLeaf(this.startWorkAction);
        }

//        if (this.getStatus() != null
//                && !(this.getStatus().equals(WorkRunStatus.DISTRIBUTED) || this.getStatus().equals(WorkRunStatus.PENDING))) {
        this.setInstanceid(format());
//        }

		/* 检查配置是否符合要求,并转换参数 */
        checkAndConvert();
    }

    private boolean checkBuild() {
        if (this.startWorkAction.listChildActions().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 运行workFlow(包含里面的actions)
     *
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    public void start() throws ParseActionException, UnknownHostException, InterruptedException {

        this.startTime = System.currentTimeMillis();
        this.runTime = System.currentTimeMillis();
        this.workDaoService.addWorkFlowInstance(this, null);
        this.startWorkAction.statusTransfer(startWorkAction, WorkFlowEvent.SUBMIT);
        this.updateWorkFlowStatus(WorkRunStatus.RUNNING);
    }

    public void startlocalWorkFlowInstance()
            throws ParseActionException, UnknownHostException, InterruptedException {

        this.startTime = System.currentTimeMillis();
        this.runTime = System.currentTimeMillis();
        this.startWorkAction.statusTransfer(startWorkAction, WorkFlowEvent.SUBMIT);
    }

    public void startWorkFlowInstance(WorkFlowService workFlowService)
            throws ParseActionException, CloneNotSupportedException, UnknownHostException,
            NoParamException, ParamDataTypeException, InterruptedException {

        this.startTime = System.currentTimeMillis();
        this.runTime = System.currentTimeMillis();
        setWorkFlowRelations(workFlowService);
        createActionInstances();
        startWorkAction.statusTransfer(startWorkAction, WorkFlowEvent.SUBMIT);
    }

    private void createActionInstances() {
        try {
            this.getWorkDaoService().addActionInstance(this.startWorkAction);
            this.startWorkAction.setSchedulerid(this.getSchedulerid());
            for (WorkActionBase action : this.actions) {
                action.setSchedulerid(this.getSchedulerid());
                this.getWorkDaoService().addActionInstance(action);
            }
            this.endWorkAction.setSchedulerid(this.getSchedulerid());
            this.getWorkDaoService().addActionInstance(this.endWorkAction);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void startSchedulerFlowInstance(WorkSchedulerService workSchedulerService)
            throws InterruptedException {
        this.startTime = System.currentTimeMillis();
        this.runTime = System.currentTimeMillis();
        /**
         * 创建schedulerInstance
         */
        try {
            workSchedulerService.dealSchedulerInstance(this);
            appendSchedulerRelation(workSchedulerService);
            createActionInstances();
            workSchedulerService.updateSchedulerStatus(this, WorkRunStatus.RUNNING);
            startWorkAction.statusTransfer(startWorkAction, WorkFlowEvent.SUBMIT);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private void appendSchedulerRelation(WorkSchedulerService workSchedulerService) {
        /**
         * 运行时,是以右边的scheduler实例为主
         */
        WorkSchedulerInstance rightInstance = workSchedulerService.querySchedulerInstance(this.instanceid);
        if (rightInstance.getConfigParamJSON() != null) {
            addSchedulerParam(rightInstance);
        }
        this.startWorkAction.setSchedulerid(rightInstance.getInstanceid());
        for (WorkActionBase action : this.actions) {
            action.setSchedulerid(rightInstance.getInstanceid());
        }
        this.endWorkAction.setSchedulerid(rightInstance.getInstanceid());
        this.workSchedulerService = workSchedulerService;
        if (rightInstance != null && rightInstance.getRelationsJson() != null) {
            String relationsJson = rightInstance.getRelationsJson();
            Map<String, WorkActionRelation> relationMap = JSONObject.parseObject(relationsJson, new TypeReference<Map<String, WorkActionRelation>>() {
            });
            AcceptSchedulerObject rightConfig = workSchedulerService.querySchedulerConfig(rightInstance.getSchedulerName());
            List<Bundle> bundles = rightConfig.getBundles();
            Long rightInstanceVersion = rightInstance.getVersion();
            logger.info(" workFlow的id:{}, 名称:{}, 定时任务实例id:{} ", this.instanceid, this.flowName, this.schedulerid);
            for (String key : relationMap.keySet()) {
                WorkActionBase workActionBase = this.getWorkActionByName(key);
                WorkActionRelation workActionRelation = relationMap.get(key);
                List<RemoteWorkAction> remoteDepends = workActionRelation.getRemoteDepends();
                if (remoteDepends != null && remoteDepends.size() > 0) {
                    workActionBase.getRemoteDepend().addAll(remoteDepends);
                }
                List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
                if (dependWorkActions != null && dependWorkActions.size() > 0) {
                    for (int i = dependWorkActions.size() - 1; i <= 0; i--) {
                        for (Bundle bundle : bundles) {
                            long calculate = bundle.getCalculate();
                            long leftVersion = workSchedulerService.calculatoRemoteVersion(rightInstanceVersion, calculate);
                            WorkSchedulerInstance leftSchedulerInstance = workSchedulerService.querySchedulerByInstanceidAndVersion(dependWorkActions.get(i).getSchedulerid(), leftVersion);
                            String wfInstanceid = leftSchedulerInstance.getWfInstanceid();
                            if (wfInstanceid != null) {
                                WorkActionInstance actionBase = this.workDaoService.queryActionByWFidAndActionName(wfInstanceid, dependWorkActions.get(i).getRemoteName());
                                if (actionBase.getStatus().equals(WorkRunStatus.SUCCESS)) {
                                    dependWorkActions.remove(i);
                                }
                            }
                        }
                    }
                    workActionBase.getDependendWorkActions().addAll(dependWorkActions);
                }
            }
            workSchedulerService.updateRelations(this.instanceid, JSONObject.toJSONString(relationMap));
        }
    }

    private void addSchedulerParam(WorkSchedulerInstance rightInstance) {
        String paramsJson = rightInstance.getParamsJson();
        if (paramsJson != null) {
            JSONArray jsonArray = JSONObject.parseObject(paramsJson).getJSONArray("workFlowConfig");
            rightInstance.parseRunParam(jsonArray, this);
        }
    }

    public void setWorkFlowRelations(WorkFlowService workFlowService)
            throws ParseActionException, CloneNotSupportedException, NoParamException,
            ParamDataTypeException, UnknownHostException {

        Map<String, WorkActionRelation> relations = this.getRelations();
        for (String key : relations.keySet()) {
            WorkActionRelation workActionRelation = relations.get(key);
            List<RemoteWorkAction> remoteDepends = workActionRelation.getRemoteDepends();
            if (remoteDepends != null && remoteDepends.size() > 0) {
                for (RemoteWorkAction remoteDepend : remoteDepends) {
                    String workFlowName = remoteDepend.getWorkFlowName();
                    if (workFlowName != null) {
                        remoteDepend.setRemoteActionName(workActionRelation.getActionName());
                        remoteDepend.setRemoteWorkFlowName(this.flowName);
                        remoteDepend.setDependWorkFlowName(workFlowName);
                        remoteDepend.setRemoteDependName(remoteDepend.getWorkActionName());
                        WorkActionBase workAction = this.getWorkActionByName(workActionRelation.getActionName());
                        workAction.addRemoteDepend(remoteDepend);
                        if (this.schedulerid == null) {
                            setProperty(workFlowService, workFlowName);
                        } else {
                            /**
                             * 需要排除掉scheduler_relation里面的workFlow,
                             * 再把剩余的workFlow创建出workFlowInstance放入workFlowInstance表中
                             */
                            workFlowService.setSchedulerPropertyByRemote(this, remoteDepend);
                        }
                    }
                }
            }

            List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
            if (dependWorkActions != null && dependWorkActions.size() > 0) {
                for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                    String workFlowName = dependWorkAction.getWorkFlowName();
                    if (workFlowName != null) {
                        dependWorkAction.setRemoteName(workActionRelation.getActionName());
                        WorkActionBase workAction = this.getWorkActionByName(workActionRelation.getActionName());
                        workAction.addDepencyWorkAction(dependWorkAction);
                        if (this.schedulerid == null) {
                            setProperty(workFlowService, workFlowName);
                        } else {
                            /**
                             * 需要排除掉scheduler_relation里面的workFlow,
                             * 再把剩余的workFlow创建出workFlowInstance放入workFlowInstance表中
                             */
                            workFlowService.setSchedulerPropertyByRemote(this, dependWorkAction);
                        }
                    }
                }
            }
        }
    }

    public void setProperty(WorkFlowService workFlowService, String workFlowName) throws ParseActionException, CloneNotSupportedException, NoParamException, ParamDataTypeException, UnknownHostException {
        WorkFlowInstance workFlowInstances = this.workDaoService.selectWFByFatherId(
                this.getFatherInstanceId(),
                workFlowName);
        if (workFlowInstances == null) {
            WorkFlow workFlow = this.workDaoService.getWorkFlowByRelation(workFlowName, this.getFlowName());
            if (workFlow == null) {
                throw new ParseActionException("can not find work flow config for name:" + flowName);
            }
            workFlow.initialize();
            workFlow.setStatus(WorkRunStatus.DISTRIBUTED);
            workFlow.getEndWorkAction().setWorkFlowService(workFlowService);
            workFlow.setRunTime(System.currentTimeMillis());
            workFlow.setStartTime(System.currentTimeMillis());
            workFlow.build();
            workDaoService.addWorkFlowInstance(workFlow, this.fatherInstanceId);
        }
    }

    public void updateWorkFlowStatus(WorkRunStatus status) throws ParseActionException, UnknownHostException {
        this.setStatus(status);
        this.setEndTime(System.currentTimeMillis());
        this.workDaoService.updateWorkFlowInstanceStatus(this);
    }


    @Override
    public void initialize() {

        if (this.getFlowName() == null) {
            throw new RuntimeException("work flow name is not set,please check");
        }
        try {
            this.initialize(this.getFlowName(), null);
        } catch (NoParamException | ParamDataTypeException e) {
            logger.error("初始化workFlow报错: ", e);
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public WorkFlow clone() throws CloneNotSupportedException {

        WorkFlow flow = (WorkFlow) super.clone();
        flow.initialize();
        return flow;

    }

    /**
     * 状态控制器
     *
     * @param event
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    public void handle(WorkFlowEvent event) throws ParseActionException, UnknownHostException, InterruptedException {
        switch (event) {

            case RESUME:

                startWorkAction.loopResume(startWorkAction);
                break;

            case KILL:

                //kill全部动作
                startWorkAction.changeAllActionStatus(WorkRunStatus.KILLED, event);
                this.updateWorkFlowStatus(WorkRunStatus.KILLED);
                break;

            case FAIL:

                startWorkAction.statusTransfer(startWorkAction, event);
                //并非停止所有动作
                startWorkAction.loopFail(startWorkAction);
                break;

            case PAUSE://暂停

                startWorkAction.statusTransfer(startWorkAction, event);
                //暂停所有动作
                startWorkAction.changeAllActionStatus(WorkRunStatus.PAUSE, event);
                break;

            case SUBMIT:

                start();
                break;

            default:
                break;

        }
    }

    public StartWorkAction getStartWorkAction() {
        return startWorkAction;
    }

    public void setStartWorkAction(StartWorkAction startWorkAction) {
        this.startWorkAction = startWorkAction;
    }

    public EndWorkAction getEndWorkAction() {
        return endWorkAction;
    }

    public void setEndWorkAction(EndWorkAction endWorkAction) {
        this.endWorkAction = endWorkAction;
    }

    public WorkRunStatus getStatus() {
        return this.status;
    }

    public StartWorkAction takeStartAction() {
        return startWorkAction;
    }

    public EndWorkAction takeEndAction() {
        return endWorkAction;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getSchedulerid() {
        return schedulerid;
    }

    public void setSchedulerid(String schedulerid) {
        this.schedulerid = schedulerid;
    }

    public List<WorkActionBase> getActions() {
        return actions;
    }

    public Map<String, WorkActionRelation> getRelations() {
        return relations;
    }

    public void setActions(List<WorkActionBase> actions) {
        this.actions = actions;
    }

    public void setRelations(Map<String, WorkActionRelation> relations) {
        this.relations = relations;
    }

    public long getConfigerid() {
        return configerid;
    }

    public void setConfigerid(long configerid) {
        this.configerid = configerid;
    }

    public long getUpdaterid() {
        return updaterid;
    }

    public void setUpdaterid(long updaterid) {
        this.updaterid = updaterid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public long getSubmiter() {
        return submiter;
    }

    public void setSubmiter(long submiter) {
        this.submiter = submiter;
    }

    public WorkDaoService getWorkDaoService() {
        return workDaoService;
    }

    public void setWorkDaoService(WorkDaoService workDaoService) {
        this.workDaoService = workDaoService;
    }

    public void setStatus(WorkRunStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WorkFlow{" +
                "runTime=" + runTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", schedulerid=" + schedulerid +
                ", instanceid='" + instanceid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", updaterid=" + updaterid +
                ", submiter=" + submiter +
                ", status=" + status +
                ", workDaoService=" + workDaoService +
                ", endWorkAction=" + endWorkAction +
                ", startWorkAction=" + startWorkAction +
                ", flowName='" + flowName + '\'' +
                ", configerid=" + configerid +
                ", actions=" + actions +
                ", relations=" + relations +
                '}';
    }

    public int getTyrTime() {
        return tyrTime;
    }

    public void setTyrTime(int tyrTime) {
        this.tyrTime = tyrTime;
    }

    private int time = 1;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<RemoteWorkFlow> getRemoteWorkFlows() {
        return remoteWorkFlows;
    }

    public void setRemoteWorkFlows(List<RemoteWorkFlow> remoteWorkFlows) {
        this.remoteWorkFlows = remoteWorkFlows;
    }

    public WorkSchedulerService getWorkSchedulerService() {
        return workSchedulerService;
    }

    public void setWorkSchedulerService(WorkSchedulerService workSchedulerService) {
        this.workSchedulerService = workSchedulerService;
    }

    public String getDependName() {
        return dependName;
    }

    public void setDependName(String dependName) {
        this.dependName = dependName;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ReferenceConfig<IWebExecutorController> dubboComsuer(String methodName) {
        ApplicationConfig ac = new ApplicationConfig();
        ac.setName("consumer");
        ReferenceConfig<IWebExecutorController> ref = new ReferenceConfig<>();
        ref.setInterface(IWebExecutorController.class);
        ref.setApplication(ac);
        ref.setVersion("1.0.0");
        MethodConfig mc = new MethodConfig();
        mc.setAsync(false);
        mc.setName(methodName);
        ref.setMethods(Arrays.asList(new MethodConfig[]{mc}));
        return ref;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getUpdateUUID() {
        return updateUUID;
    }

    public void setUpdateUUID(String updateUUID) {
        this.updateUUID = updateUUID;
    }

    public void setRunParamJson(String runParamJson) {
        this.runParamJson = runParamJson;
    }

    public String getRunParamJson() {
        return runParamJson;
    }

}