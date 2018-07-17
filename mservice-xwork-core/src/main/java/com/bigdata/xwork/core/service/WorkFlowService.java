package com.bigdata.xwork.core.service;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.banggood.xwork.api.entity.LineResult;
import com.banggood.xwork.core.common.*;
import com.bigdata.xwork.action.core.*;
import com.bigdata.xwork.core.common.*;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.entity.*;
import com.bigdata.xwork.dao.entity.*;
import com.bigdata.xwork.dao.mapper.WorkActionInstanceMapper;
import com.bigdata.xwork.dao.mapper.WorkFlowInfoMapper;
import com.bigdata.xwork.dao.mapper.WorkFlowInstanceMapper;
import com.bigdata.xwork.dao.service.WorkDaoService;
import com.bigdata.xwork.query.WorkFlowConfigQueryObject;
import com.bigdata.xwork.query.WorkFlowInstanceQueryObject;
import com.bigdata.xwork.query.WorkFlowNameQueryObject;
import com.bigdata.xwork.query.result.ConfigResult;
import com.bigdata.xwork.query.result.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.*;

@Service
public class WorkFlowService {

    @Autowired
    private WorkDaoService workDaoService;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowInstanceMapper workFlowInstanceMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowInfoMapper workFlowInfoMapper;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkActionInstanceMapper workActionInstanceMapper;
    @Autowired
    private ServiceRegistry register;
    @Autowired
    private FlowActionUtils flowActionUtils;

    public static Map<String, WorkFlow> cachedFlowInstance = new HashMap<>();

    public static Map<String, WorkFlow> pendingFlowInstance = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(WorkFlowService.class);

    public WorkFlow getWorkFlow(String flowName) throws ParseActionException, CloneNotSupportedException {
        return this.workDaoService.getWorkFlow(flowName);
    }

    public void addWorkFlowInstance(WorkFlow flow) {
        WorkFlowService.cachedFlowInstance.put(flow.getInstanceid(), flow);
    }

    public WorkFlow getWorkFlowInstance(String instanceid) {
        if (cachedFlowInstance.containsKey(instanceid)) {
            return cachedFlowInstance.get(instanceid);
        } else {
            logger.info("can not find work flow instance by instanceid: {}", instanceid);
            return null;
        }
    }

    public void removeWorkFlowInstance(String instanceid) {
        if (WorkFlowService.cachedFlowInstance.containsKey(instanceid)) {
            logger.info("remove work flow instance by instanceid: {}", instanceid);
            WorkFlowService.cachedFlowInstance.remove(instanceid);
        }
    }

    public PageResult getWorkFlowAll(WorkFlowConfigQueryObject qo) {

        int count = this.workFlowInfoMapper.queryForCount(qo);
        if (count > 0) {
            List<WorkFlowInfo> workFlowAll = this.workFlowInfoMapper.getWorkFlowAll(qo);
            return new PageResult(workFlowAll, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    /**
     * 解析运行参数(workFlow级别的)
     *
     * @param parameters
     * @param workFlow
     */
    public void parseRunParam(JSONArray parameters, WorkFlow workFlow) {

        Map<String, Map<String, String>> actionParam = parseRunParam(parameters);

        for (String actionName : actionParam.keySet()) {
            WorkActionBase action = workFlow.getWorkActionByName(actionName);
            Map<String, String> argumentMaps = actionParam.get(actionName);
            if (action != null) {
                action.putActionsArguments(argumentMaps);
            } else {
                logger.error("参数传递错误,参数action的名称填错: {}", actionName);
                throw new RuntimeException("参数传递错误,参数action的名称填错: " + actionName);
            }
            logger.info("参数: {} ", argumentMaps);
        }
        workFlow.setArguments(actionParam);
    }

    public Map<String, Map<String, String>> parseRunParam(JSONArray parameters) {
        Map<String, Map<String, String>> actionParam = new HashMap<>(5);
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject jsonObject = parameters.getJSONObject(i);
            String actionName = jsonObject.getString("actionName");
            Map<String, String> actionMap = actionParam.get(actionName);
            if (actionMap == null) {
                actionMap = new HashMap<>(5);
                actionParam.put(actionName, actionMap);
            }
            Object key = jsonObject.get("key");
            if (key != null && !"".equals(key)) {
                actionMap.put(jsonObject.getString("key"), jsonObject.getString("value"));
            }
        }
        return actionParam;
    }

    /**
     * 确定是否有远程调用的workFlow
     *
     * @param workFlow
     * @return
     */
    public boolean checkIsRemote(WorkFlow workFlow) {
        Map<String, WorkActionRelation> relations = workFlow.getRelations();
        for (String workActionName : relations.keySet()) {
            WorkActionRelation workActionRelation = relations.get(workActionName);
            List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
            if (dependWorkActions != null && dependWorkActions.size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 找出所有需要依赖的workFlow
     *
     * @param workFlow
     * @param workFlowNames
     * @throws ParseActionException
     * @throws CloneNotSupportedException
     */
    public void loopCheckWorkFlowName(WorkFlow workFlow, List<String> workFlowNames)
            throws ParseActionException, CloneNotSupportedException {
        Map<String, WorkActionRelation> relations = workFlow.getRelations();
        for (String workActionName : relations.keySet()) {
            WorkActionRelation workActionRelation = relations.get(workActionName);
            List<DependencyWorkAction> dependWorkActions = workActionRelation.getDependWorkActions();
            if (dependWorkActions != null && dependWorkActions.size() > 0) {
                for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                    String workFlowName = dependWorkAction.getWorkFlowName();
                    if (workFlowNames.size() > 0 && !workFlowNames.contains(workFlowName)) {
                        workFlowNames.add(workFlowName);
                        WorkFlow wf = getWorkFlow(workFlowName);
                        loopCheckWorkFlowName(wf, workFlowNames);
                    }
                }
            }
        }
    }

    public List<String> remote(String[] workFlowNames, Boolean open, WorkFlow w)
            throws NoParamException, ParseActionException, ParamDataTypeException,
            CloneNotSupportedException {
        Map<String, WorkFlow> maps = new HashMap<>(workFlowNames.length);
        if (w != null) {
            maps.put(w.getFlowName(), w);
        }
        for (String flowName : workFlowNames) {
            WorkFlow workFlow = null;
            if (maps.get(flowName) != null) {
                workFlow = maps.get(flowName);
            } else {
                workFlow = this.workDaoService.getWorkFlow(flowName);
                maps.put(workFlow.getFlowName(), workFlow);
                workFlow.initialize();
                workFlow.setStatus(WorkRunStatus.DISTRIBUTED);
                // workFlow.getEndWorkAction().setWorkFlowService(workFlowService);
                workFlow.setRunTime(System.currentTimeMillis());
                workFlow.setStartTime(System.currentTimeMillis());
                workFlow.build();
            }
            // String ip = this.serviceRegistry.discover();
            String ip = "";
            workFlow.setIp(ip);
            Map<String, List<DependencyWorkAction>> remoteMap = new HashMap<>();
            List<DependencyWorkAction> dependcyWorkActions = workFlow.getDependcyWorkActions();
            for (DependencyWorkAction dependcyWorkAction : dependcyWorkActions) {
                if (remoteMap.get(dependcyWorkAction.getWorkFlowName()) == null
                        || remoteMap.get(dependcyWorkAction.getWorkFlowName()).isEmpty()) {
                    List<DependencyWorkAction> dwas = new ArrayList<>();
                    dwas.add(dependcyWorkAction);
                    remoteMap.put(dependcyWorkAction.getWorkFlowName(), dwas);
                } else {
                    remoteMap.get(dependcyWorkAction.getWorkFlowName()).add(dependcyWorkAction);
                }
            }
            for (String workFlowName : remoteMap.keySet()) {
                this.parseRemoteMap(workFlow, ip, remoteMap, workFlowName, maps);
            }
        }

        List<RemoteWorkFlow> rwfs = new ArrayList<>();
        /**
         * 保存好所有有相关需要远程调用的workFlow,主要用于失败时,循环这个集合,告诉所有的workFlow失败了
         */
        for (String key : maps.keySet()) {
            WorkFlow workFlow = maps.get(key);
            RemoteWorkFlow remoteWorkFlow = new RemoteWorkFlow();
            remoteWorkFlow.setRemoteIp(workFlow.getIp());
            remoteWorkFlow.setRemoteWorkFlowInstanceId(workFlow.getInstanceid());
            remoteWorkFlow.setSubmit(true);
            rwfs.add(remoteWorkFlow);
        }
        for (String key : maps.keySet()) {
            WorkFlow workFlow = maps.get(key);
            workFlow.setRemoteWorkFlows(rwfs);
        }

        List<String> instanceids = new ArrayList<>();
        for (String key : maps.keySet()) {
            WorkFlow workFlow = maps.get(key);
            /**
             * 远程启动workFlow
             */
            EventResponse result = dealNode(workFlow.getIp(), workFlow);
            instanceids.add(workFlow.getInstanceid());
            dealWithRespone(result);
        }
        return instanceids;
    }

    public List<String> remote(String[] workFlowNames, Boolean open)
            throws NoParamException, ParseActionException, ParamDataTypeException,
            CloneNotSupportedException {
        return this.remote(workFlowNames, open, null);
    }

    private void parseRemoteMap(WorkFlow workFlow,
                                String ip,
                                Map<String, List<DependencyWorkAction>> remoteMap,
                                String workFlowName,
                                Map<String, WorkFlow> maps)
            throws ParseActionException, CloneNotSupportedException, NoParamException,
            ParamDataTypeException {
        List<DependencyWorkAction> dependencyWorkActions = remoteMap.get(workFlowName);
        WorkFlow wf;
        if (maps.get(workFlowName) != null) {
            wf = maps.get(workFlowName);
        } else {
            wf = this.workDaoService.getWorkFlow(workFlowName);
            wf.initialize();
            wf.setStatus(WorkRunStatus.DISTRIBUTED);
            wf.getEndWorkAction().setWorkFlowService(workFlowService);
            wf.setRunTime(System.currentTimeMillis());
            wf.setStartTime(System.currentTimeMillis());
            wf.build();
//          String discover = this.serviceRegistry.discover();
//          wf.setIp(discover);
        }

        for (DependencyWorkAction dependencyWorkAction : dependencyWorkActions) {
            WorkActionBase workAction =
                    wf.getWorkActionByName(dependencyWorkAction.getWorkActionName());
            RemoteWorkAction remoteDepend = new RemoteWorkAction();
            remoteDepend.setIp(ip);
            remoteDepend.setRemoteActionName(workAction.getActionName());//
            remoteDepend.setWorkFlowInstanceid(workFlow.getInstanceid());//

            remoteDepend.setRemoteDependName(dependencyWorkAction.getRemoteName());//
            //  System.out.println("---------------->" + remoteDepend.toString());
            workAction.addRemoteDepend(remoteDepend);
        }
        //   System.out.println("{{{{{{{{{{{{" + wf.getFlowName() + "}}}}}}}}}}}}}");
        maps.put(wf.getFlowName(), wf);
//      System.out.println("remoteA:" + url);
//      String result = dealNode(url, wf);
//      String result = new OkHTTPUtil().httpRequstPost(url, JSONObject.toJSONString(wf), null);
//      dealWithRespone(result);
    }

    private void checkRemoteInfiniteLoops(WorkFlow workFlow)
            throws ParseActionException, CloneNotSupportedException {
        List<DependencyWorkAction> dwas = workFlow.getDependcyWorkActions();
        for (DependencyWorkAction dwa : dwas) {
            String remoteName = dwa.getRemoteName();
            this.remoteLoops(remoteName, dwa);
        }
    }

    private void remoteLoops(String remoteName, DependencyWorkAction dwa)
            throws ParseActionException, CloneNotSupportedException {
        WorkFlow workFlow = this.workDaoService.getWorkFlow(dwa.getWorkFlowName());
        DependencyWorkAction dependcyWorkAction = workFlow.getDependcyWorkActionByName(dwa.getWorkActionName());
        if (dependcyWorkAction != null) {
            this.remoteLoops(null, dwa);
        } else {
            String workActionName = dwa.getWorkActionName();
            WorkActionBase workAction = workFlow.getWorkActionByName(workActionName);
            List<WorkActionBase> fatherActions = workAction.listFatherActions();
            for (WorkActionBase fatherAction : fatherActions) {
                List<DependencyWorkAction> dependendWorkActions = fatherAction.getDependendWorkActions();
                if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                    for (DependencyWorkAction dependendWorkAction : dependendWorkActions) {
                        this.remoteLoops(null, dwa);
                    }
                } else {
                    this.fatherLoops(fatherAction, remoteName);
                }
            }
        }
    }

    private void fatherLoops(WorkActionBase fatherAction, String remoteName)
            throws ParseActionException, CloneNotSupportedException {
        for (WorkActionBase actionBase : fatherAction.listFatherActions()) {
            List<DependencyWorkAction> dependendWorkActions = actionBase.getDependendWorkActions();
            if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                for (DependencyWorkAction dependendWorkAction : dependendWorkActions) {
                    this.remoteLoops(null, dependendWorkAction);
                }
            } else {
                if (fatherAction.getActionName().equals(remoteName)) {
                    throw new RuntimeException("remote action dowm in infiniteLoops");
                }
                if (fatherAction.listFatherActions() != null && fatherAction.listFatherActions().size() > 0) {
                    this.fatherLoops(fatherAction, remoteName);
                }
            }
        }
    }

    private void checkDependWorkAction(WorkFlow workFlow)
            throws ParseActionException, CloneNotSupportedException {
        List<DependencyWorkAction> dependWorkActions = workFlow.getDependcyWorkActions();
        if (dependWorkActions != null && dependWorkActions.size() > 0) {
            for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                String workFlowName = dependWorkAction.getWorkFlowName();
                WorkFlow wf = this.workDaoService.getWorkFlow(workFlowName);
                WorkActionBase workActionByName = wf.getWorkActionByName(dependWorkAction.getWorkActionName());
                if (workActionByName == null) {
                    throw new RuntimeException(" there is not workAction ");
                }
//                if(workFlow.getFlowName().equals(workFlowName)){
//                    throw new RuntimeException(" it can not remote yourself");
//                }
            }
        }
    }

    private EventResponse dealNode(String url, WorkFlow wf) {
        EventResponse result = null;
        try {
            ReferenceConfig<IWebExecutorController> ref = this.dubboComsuer("remote");
            ref.setUrl(url);
            result = ref.get().remote(JSONObject.toJSONString(wf));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("this node is dead : {}", url);
            throw new RuntimeException("this node is dead :" + url);
        }
        return result;
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

    private void dealWithRespone(EventResponse result) {

        if (result.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            logger.error(result.getDesc());
            throw new RuntimeException(result.getDesc());
        }
    }

    public boolean deleteWorkFlowInfo(String[] workFlowName) {
        for (String wfn : workFlowName) {
            if (!this.workFlowInfoMapper.deleteWorkFlowByName(wfn)) {
                return false;
            }
        }
        return true;
    }


    public PageResult query(WorkFlowInstanceQueryObject qo) {
        int count = this.workFlowInstanceMapper.queryForCount(qo);
        if (count > 0) {
            List<WorkFlowInstance> workFlowInstances = this.workFlowInstanceMapper.query(qo);
            return new PageResult(workFlowInstances, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    public boolean deleteWorkFlowInstance(String[] instanceid) {
        for (String id : instanceid) {
            if (!this.workFlowInstanceMapper.delete(id)) {
                return false;
            }
        }
        return true;
    }

    public PageResult queryWorkFlowConfigByName(WorkFlowNameQueryObject qo) {
        int count = this.workFlowInfoMapper.queryByNameForCount(qo.getWorkFlowName());
        if (count > 0) {
            List<WorkFlowInfo> workFlowInfos = this.workFlowInfoMapper.queryByName(qo);
            return new PageResult(workFlowInfos, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    public List<WorkActionInstance> queryActionInstanceByWFName(String instanceid) {
        List<WorkActionInstance> workActionInstances = this.workActionInstanceMapper.queryActionInstancesForInstanceId(instanceid);
        for (int i = 0; i < workActionInstances.size(); i++) {
            if ("END".equals(workActionInstances.get(i).getActionName()) || "START".equals(workActionInstances.get(i).getActionName())) {
                workActionInstances.remove(i);
            }
        }
        return workActionInstances;
    }

    public WorkFlowInfo getWorkInfoOneByName(String workFlowName) {
        return this.workFlowInfoMapper.getWorkActionFlowByName(workFlowName);
    }

    public void checkFlowName(String flowName) throws ParseActionException {
        WorkFlowInfo workFlowInfo = this.workFlowInfoMapper.getWorkActionFlowByName(flowName);
        if (workFlowInfo == null && flowName != null) {
            throw new ParseActionException("can not find work flow config for name:" + flowName);
        }
    }

    public String createWorkFlowInstanceId(String flowName, String uuid, long rumTime) {
        return new StringBuilder(50).append("wf_").append(flowName).append("_")
                .append(uuid).append("_").append(rumTime).append("_").append("0").toString();
    }

    public WorkFlowInfo queryRequestObj(String flowName) {
        return this.workFlowInfoMapper.getWorkActionFlowByName(flowName);
    }

    public void setSchedulerPropertyByRemote(WorkFlow workFlow, WorkActionTransfer workAction)
            throws ParseActionException, CloneNotSupportedException, NoParamException,
            UnknownHostException, ParamDataTypeException {


    }

    public int queue() throws UnsupportedEncodingException {
        List<String> nodes = register.getNodes();
        int count = 0;
        for (String node : nodes) {
            ReferenceConfig<IWebExecutorController> ref = workFlowService.dubboComsuer("queue");
            ref.setUrl(node);
            int num = ref.get().queue();
            count = count + num;
        }
        return count;
    }

    public int getWorkFlowForCount(String flowName) {
        return this.workFlowInfoMapper.getWorkFlowForCount(flowName);
    }

    public void realDeWF(String flowName) {
        this.workFlowInfoMapper.realDeByName(flowName);
    }

    public String getWorkFlowInstanceConfig(String instanceId) {
        WorkFlowInstance workFlowInstance = this.workFlowInstanceMapper.selectWorkInstanceByid(instanceId);
        if (workFlowInstance == null) {
            return "找不到 workFlowInstance";
        }
        return workFlowInstance.getWorkFlowInfoJSON();
    }

    public WorkFlowInstance queryWorkFlowInstanceById(String instanceId) {
        return this.workFlowInstanceMapper.selectWorkInstanceByid(instanceId);
    }

    public List<WorkFlowInfo> queryWorkFlowByName(String workFlowName) {
        List<WorkFlowInfo> workFlowInfos = this.workFlowInfoMapper.queryWorkFlowByName(workFlowName);
        for (WorkFlowInfo info : workFlowInfos) {
            StringBuilder sb = new StringBuilder(50);
            JSONObject object = JSONObject.parseObject(info.getRelationsJson());
            Set<String> keySet = object.keySet();
            for (String actionName : keySet) {
                sb.append(actionName).append(",");
            }
            String actions = sb.toString();
            info.setShowActions(actions.substring(0, actions.length() - 1));
        }
        return workFlowInfos;
    }

    public List<WorkActionBase> queryWorkFlowByActionName(String flowName) throws ParseActionException, CloneNotSupportedException {
        WorkFlowInfo info = this.workFlowInfoMapper.getWorkActionFlowByName(flowName);

        return flowActionUtils.encodeWorkFlow(JSONObject.parseArray(info.getActions(), WorkActionInfo.class));
    }

    public List<ConfigResult> configWorkFlow(String workFlowInstance) throws ParseActionException, CloneNotSupportedException {
        List<ConfigResult> configResults = new ArrayList<>(6);
        WorkFlowInstance wfinstance = this.workFlowInstanceMapper.selectWorkInstanceByid(workFlowInstance);
        WorkFlow workFlow = getWorkFlow(wfinstance.getFlowName());
        ConfigResult conf = new ConfigResult();
        conf.setName("普通任务流名称");
        conf.setValue(wfinstance.getFlowName());
        configResults.add(conf);

        conf = new ConfigResult();
        conf.setName("运行实例的id");
        conf.setValue(wfinstance.getInstanceid());
        configResults.add(conf);

        List<WorkActionBase> actions = workFlow.getActions();
        for (WorkActionBase action : actions) {
            Map<String, WorkActionParam> parameters = action.getConfig().getParameters();
            StringBuilder sb = new StringBuilder(50);
            for (String key : parameters.keySet()) {
                sb.append("[").append(key).append(" : ").append(parameters.get(key).getContent()).append("]");
            }
            conf = new ConfigResult();
            conf.setName(action.getActionName());
            conf.setValue(sb.toString());
            configResults.add(conf);
        }
        conf = new ConfigResult();
        conf.setName("配置描述");
        conf.setValue(workFlow.getDescription());
        configResults.add(conf);
        return configResults;
    }

    public void resumeOneAction(String id) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InterruptedException {
        WorkActionInstance actionInstance = this.workActionInstanceMapper.queryByActionId(id);
        String clazz = actionInstance.getClazz();
        Class<? extends WorkActionBase> cl = (Class<? extends WorkActionBase>) Class.forName(clazz);
        WorkActionBase action = cl.newInstance();
        action.setActionName(actionInstance.getActionName());
        action.setClazz(cl);
        action.setWorkDaoService(workDaoService);
        action.setInstanceid(actionInstance.getInstanceid());
        action.setStatus(WorkRunStatus.DISTRIBUTED);
        JSONObject jsonObject = JSONObject.parseObject(actionInstance.getRunParamJSON());
        Map<String, WorkActionParam> paramMap = jsonObject.getObject("parameters", new TypeReference<Map<String, WorkActionParam>>() {
        });
        WorkConfig workConfig = new WorkConfig();
        workConfig.setParameters(paramMap);
        action.setConfig(workConfig);
        action.setShare(false);
        action.setActionType(WorkActionType.NORMORL);
        action.setStartTime(System.currentTimeMillis());
        action.setEndTime(System.currentTimeMillis());
        action.setRunTime(System.currentTimeMillis());
        action.beforeStart();
        this.workActionInstanceMapper.updateStatus(actionInstance.getInstanceid(), actionInstance.getStatus(), actionInstance.getEndTime(), actionInstance.getRunTime(), actionInstance.getStartTime(), actionInstance.getOutPutJSON());
        WorkExecutorPool.executorService.submit(action);

    }

    public LineResult queryWorkFlowLogger(String instanceid, long size) {
        String url = this.workFlowInstanceMapper.queryWorkFlowInstanceIp(instanceid);
        ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("queryWorkFlowLogger");
        ref.setUrl(url);
        return ref.get().queryWorkFlowLogger(instanceid, size);
    }
}
