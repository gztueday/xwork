package com.banggood.xwork.core.common;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.ExecutorDispatcher;
import com.banggood.xwork.core.service.WorkActionService.WorkActionDescription;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.banggood.xwork.scheduler.core.WorkScheduler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FlowActionUtils {

    private final static Logger logger = LoggerFactory.getLogger(FlowActionUtils.class);
    private final String CRON_DATE_FORMAT = "ss mm HH dd MM ? yyyy";
    @Autowired
    private WorkFlowService workFlowService;

    public String getCron(final Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
        String formatTimeStr = "";
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }

    public Date getDate(final String cron) {
        if (cron == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
        Date date = null;
        try {
            date = sdf.parse(cron);
        } catch (ParseException e) {
            logger.error(" cron解析错误 ", e);
            return null;
        }
        return date;
    }

    public WorkActionInstance encodeWorkActionInstance(WorkActionBase action, WorkFlow workFlow) {
        WorkActionInstance instance = new WorkActionInstance();
        instance.setActionName(action.getActionName());
        instance.setClazz(action.getClass().getCanonicalName());
        instance.setConfigerid(workFlow.getConfigerid());
        instance.setConfigParamJSON(JSONObject.toJSONString(action.getConfig()));
        instance.setRunParamJSON(JSONObject.toJSONString(action.getRunParam()));
        instance.setEndTime(action.getEndTime() == null ? null : new Timestamp(action.getEndTime()));
        instance.setStartTime(action.getStartTime() == null ? null : new Timestamp(action.getStartTime()));
        instance.setRunTime(action.getRunTime() == null ? null : new Timestamp(action.getRunTime()));
        instance.setOutPutJSON(JSONObject.toJSONString(action.getOutPut()));
        instance.setSchedulerid(action.getSchedulerid());
        instance.setStatus(action.getStatus());
        instance.setInstanceid(action.getInstanceid());
        instance.setConditionPass(action.isConditionPass());
        instance.setShare(action.isShare());
        instance.setFlowName(action.getFlowName());
        instance.setFlowInstanceid(workFlow.getInstanceid());

        return instance;
    }

    /**
     * 转换运行实例信息workFlow-->WorkFlowInstance
     *
     * @throws ParseActionException
     * @throws UnknownHostException
     */
    public WorkFlowInstance encodeWorkFlowInstance(WorkFlow workFlow) throws ParseActionException, UnknownHostException {
        WorkFlowInstance instance = new WorkFlowInstance();
        instance.setConfigParamJSON(JSONObject.toJSONString(workFlow.getConfig()));
        instance.setRunParamJSON(JSONObject.toJSONString(workFlow.getRunParam()));
        instance.setFlowName(workFlow.getFlowName());
        instance.setInstanceid(workFlow.getInstanceid());

        instance.setEndTime(workFlow.getEndTime() == null ? null : new Timestamp(workFlow.getEndTime()));
        instance.setRunTime(workFlow.getRunTime() == null ? null : new Timestamp(workFlow.getRunTime()));
        instance.setStartTime(workFlow.getStartTime() == null ? null : new Timestamp(workFlow.getStartTime()));

        instance.setStatus(workFlow.getStatus());
        instance.setWorkFlowInfoJSON(JSONObject.toJSONString(encodeWorkFlowInfo(workFlow)));
        instance.setExecuteIP(ExecutorDispatcher.ip);
//        instance.setUuid(workFlow.getUuid());
        instance.setSchedulerid(workFlow.getSchedulerid());
        instance.setConfigerid(workFlow.getConfigerid());
        instance.setUpdaterid(workFlow.getUpdaterid());
        instance.setSubmiter(workFlow.getSubmiter());
        if (workFlow.getSchedulerid() != null) {
            instance.setSchedulerid(workFlow.getSchedulerid());
        }

        return instance;
    }

    public WorkFlowInstance encodeWorkFlowInstanceByRemote(WorkFlow wf)
            throws UnknownHostException, ParseActionException {
        WorkFlowInstance instance = new WorkFlowInstance();
        instance.setConfigParamJSON(JSONObject.toJSONString(wf.getConfig()));
        instance.setRunParamJSON(JSONObject.toJSONString(wf.getRunParam()));
        instance.setEndTime(new Timestamp(wf.getEndTime()));
        instance.setFlowName(wf.getFlowName());
        instance.setInstanceid(wf.getInstanceid());
        instance.setRunTime(new Timestamp(wf.getRunTime()));
        instance.setStartTime(new Timestamp(wf.getStartTime()));
        instance.setStatus(wf.getStatus());
        instance.setWorkFlowInfoJSON(JSONObject.toJSONString(encodeWorkFlowInfo(wf)));

        instance.setExecuteIP(wf.getIp());


//        instance.setUuid(wf.getUuid());
        instance.setSchedulerid(wf.getSchedulerid());
        instance.setConfigerid(wf.getConfigerid());
        instance.setUpdaterid(wf.getUpdaterid());
        instance.setSubmiter(wf.getSubmiter());
        return instance;
    }

    /**
     * WorkFlowInstance-->WorkFlow
     *
     * @throws CloneNotSupportedException
     * @throws ParseActionException
     */
    public WorkFlow decodeWorkFlowInstance(WorkFlowInstance instance)
            throws ParseActionException, CloneNotSupportedException {
        /**
         * 设置workFlow关系的方法
         */
        WorkFlow flow = decodeWorkFlowInfo(JSONObject.parseObject(instance.getWorkFlowInfoJSON(), WorkFlowInfo.class));
        flow.setConfig(JSONObject.parseObject(instance.getConfigParamJSON(), WorkConfig.class));
        flow.setRunParam(JSONObject.parseObject(instance.getRunParamJSON(), WorkConfig.class));
        flow.setFlowName(instance.getFlowName());
//        flow.setUuid(instance.getUuid());
        flow.setStatus(instance.getStatus());
        Timestamp runTime = instance.getRunTime();
        flow.setRunTime(runTime.getTime());
        flow.setSchedulerid(instance.getSchedulerid());
        flow.setConfigerid(instance.getConfigerid());
        flow.setUpdaterid(instance.getUpdaterid());
        flow.setSubmiter(instance.getSubmiter());
        flow.setInstanceid(instance.getInstanceid());
        flow.setFatherInstanceId(instance.getFatherInstanceId());
        flow.setIp(instance.getExecuteIP());
        flow.setVersion(instance.getVersions());
        flow.setSubWorkFlows(JSONObject.parseObject(instance.getSubWorkFlowJson(), new TypeReference<List<SubWorkFlow>>() {
        }));

        String runningParams = instance.getRunningParams();
        Map<String, Map<String, String>> arguments = decodeWorkRunParams(runningParams);
        flow.setArguments(arguments);
        addParamInAction(flow);
        return flow;
    }

    public void addParamInAction(WorkFlow flow) {
        Map<String, Map<String, String>> arguments = flow.getArguments();
        for (String actionName : arguments.keySet()) {
            WorkActionBase action = flow.getWorkActionByName(actionName);
            Map<String, String> argumentMaps = arguments.get(actionName);
            if (action != null) {
                action.putActionsArguments(argumentMaps);
            } else {
                logger.error(" 找不到当前的action: {}", actionName);
                throw new RuntimeException("找不到当前的action: " + actionName);
            }
            logger.info(" putActionsArguments: {} ", argumentMaps);
        }
    }

    public Map<String, Map<String, String>> decodeWorkRunParams(String runningParams) {
        return JSONObject.parseObject(runningParams, new TypeReference<Map<String, Map<String, String>>>() {
        });
    }

    /**
     * 转换配置信息WorkActionInfo--->WorkActionBase
     *
     * @throws ParseActionException
     * @throws CloneNotSupportedException
     */
    public List<WorkActionBase> encodeWorkFlow(List<WorkActionInfo> infos)
            throws ParseActionException, CloneNotSupportedException {
        List<WorkActionBase> actions = new ArrayList<>();
        for (WorkActionInfo info : infos) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends WorkActionBase> cl = (Class<? extends WorkActionBase>) Class
                        .forName(info.getActionClass());
                WorkActionBase action = cl.newInstance();
                action.setConfig(JSONObject.parseObject(info.getConfigParamJSON(), WorkConfig.class));
                action.setActionName(info.getActionName());
                action.setActionType(info.getActionType());
                action.setClazz(cl);
                action.setShare(info.isShare());
                action.setCacheRelation(JSONObject.parseObject(info.getCacheRelation(), CacheRelation.class));
                if (info.getSubWorkFlowName() != null) {
                    if (workFlowService.getWorkFlow(info.getSubWorkFlowName()) == null) {
                        throw new ParseActionException(
                                "can not found sub work flow for name:" + info.getSubWorkFlowName());
                    } else {
                        action.setSubWorkFlow(workFlowService.getWorkFlow(info.getSubWorkFlowName()));
                    }
                }

                actions.add(action);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.info("parse action error for {}", info.getActionClass());
                throw new ParseActionException("parse action error for " + info.getActionClass());
            }
        }
        return actions;

    }


    /**
     * 转换配置信息WorkFlow-->WorkFlowInfo
     */
    public WorkFlowInfo encodeWorkFlowInfo(WorkFlow flow) throws ParseActionException {

        WorkFlowInfo info = new WorkFlowInfo();
        info.setActions(JSONObject.toJSONString(decodeWorkFlowConfig(flow)));
        info.setConfigParamJSON(JSONObject.toJSONString(flow.getConfig()));
        info.setFlowName(flow.getFlowName());
        /**
         * 将workFlow的关系设置到WorkFlowInfo中以便保存到数据库中
         */
        info.setRelationsJson(JSONObject.toJSONString(flow.getRelations()));

        if (flow.getEmails() != null) {
            StringBuilder sb = new StringBuilder(50);
            for (int i = 0; i < flow.getEmails().size(); i++) {
                sb.append(flow.getEmails().get(i));
                if (i < flow.getEmails().size() - 1) {
                    sb.append(",");
                }
            }
            info.setEmailJson(sb.toString());
        }
        info.setConfigerid(flow.getConfigerid());
        info.setSubmiter(flow.getSubmiter());
        info.setUpdaterid(flow.getUpdaterid());
        info.setRequestObj(flow.getRequestObj());
        return info;
    }

    /**
     * 转换WorkFlowInfo -->WorkFlow
     * 序列化方法
     */
    @SuppressWarnings("unchecked")
    public WorkFlow decodeWorkFlowInfo(WorkFlowInfo info) throws ParseActionException, CloneNotSupportedException {

        WorkFlow flow = new WorkFlow();
        flow.setFlowName(info.getFlowName());
        flow.setDescription(info.getDescription());
        flow.setConfig(JSONObject.parseObject(info.getConfigParamJSON(), WorkConfig.class));
        flow.setUpdaterid(info.getUpdaterid());
        flow.setConfigerid(info.getConfigerid());
        flow.setSubmiter(info.getSubmiter());
        flow.setShare(info.isShare());
        if (info.getEmailJson() != null) {
            flow.setEmails(Arrays.asList(StringUtils.split(info.getEmailJson(), ",")));
        }
        flow.setActions(encodeWorkFlow(JSONObject.parseArray(info.getActions(), WorkActionInfo.class)));
        /**
         * 下面的代码是解析workFlowInstance运行时关系的代码
         *  Map<String, WorkActionRelation>表示运行关系
         */
        JSONObject object = JSONObject.parseObject(info.getRelationsJson());
        Set<String> keySet = object.keySet();
        Map<String, WorkActionRelation> map = new HashMap<>(keySet.size());
        for (String key : keySet) {
            WorkActionRelation param = object.getObject(key, WorkActionRelation.class);
            map.put(key, param);
        }
        flow.setRelations(map);
        flow.setUpdateUUID(info.getUuid());
        return flow;

    }


    /**
     * 转换持久化配置信息WorkActionBase--->WorkActionInfo
     *
     * @throws ParseActionException
     */
    private List<WorkActionInfo> decodeWorkFlowConfig(WorkFlow flow) throws ParseActionException {
        List<WorkActionBase> actions = flow.getActions();
        List<WorkActionInfo> infos = new ArrayList<>(actions.size());
        for (WorkActionBase action : actions) {

            WorkActionInfo info = new WorkActionInfo();
            info.setActionName(action.getActionName());
            info.setActionType(action.getActionType());

            info.setActionClass(action.getClass().getCanonicalName());
            info.setConfigParamJSON(JSONObject.toJSONString(action.getConfig()));
            info.setFlowName(action.getFlowName());
            info.setShare(action.isShare());
            info.setSubWorkFlowName(action.getSubWorkFlowName());

            infos.add(info);
        }

        return infos;

    }


    /**
     * 获取节点描述信息
     */
    public WorkActionDescription getWorkActionDescription(String className)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        WorkActionDescription description = new WorkActionDescription();

        @SuppressWarnings("unchecked")
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);

        WorkActionTag anno = clazz.getAnnotation(WorkActionTag.class);
        if (anno == null) {
            throw new ClassNotFoundException("not found Annotation WorkActionTag in the class " + className);
        } else {
            String desc = anno.desc();
            String name = anno.name();
            WorkActionBase action = (WorkActionBase) clazz.newInstance();
            description.setDesc(desc);
            description.setShowName(name);
            description.setActionType(action.getActionType());
            description.setConfigs(action.getConfig());
            description.setClazz(action.getClass().getCanonicalName());
        }

        return description;
    }

    public WorkActionDescription getWorkActionDescription(WorkActionBase action) {

        WorkActionDescription description = new WorkActionDescription();
        description.setActionType(action.getActionType());
        description.setConfigs(action.getConfig());
        description.setClazz(action.getClass().getCanonicalName());
        description.setActionName(action.getActionName());

        return description;
    }

    public WorkActionBase copyAction(String clazz)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (WorkActionBase) Class.forName(clazz).newInstance();
    }

    /**
     * 转换配置WorkScheduler-->WorkSchedulerInfo
     */
    public WorkSchedulerInfo encodeWorkSchedulerInfo(WorkScheduler scheduler) {
        WorkSchedulerInfo info = new WorkSchedulerInfo();

        info.setConfigParamJSON(JSONObject.toJSONString(scheduler.getConfig()));

        info.setConfigerid(scheduler.getConfigerid());

        info.setCronStr(scheduler.getCronStr());

        info.setFlowName(scheduler.getFlowName());

        info.setSchedulerName(scheduler.getSchedulerName());

//        info.setConfigerid(scheduler.getConfigerid());

        info.setSubmiter(scheduler.getSubmiter());

        info.setUpdaterid(scheduler.getUpdaterid());

        return info;
    }

    /**
     * 转换信息WorkScheduler-->WorkSchedulerInstance
     **/
    public WorkSchedulerInstance encodeWorkSchedulerInstance(WorkScheduler scheduler) {
        WorkSchedulerInstance instance = new WorkSchedulerInstance();
        instance.setStatus(WorkRunStatus.PAUSE);
        instance.setRunParamJSON(JSONObject.toJSONString(scheduler.getRunParam()));
        instance.setConfigParamJSON(JSONObject.toJSONString(scheduler.getConfig()));
        instance.setFlowName(scheduler.getFlowName());
        instance.setCronStr(scheduler.getCronStr());
        // instance.setEndTime(new Timestamp(scheduler.getEndTime()));
        instance.setStartTime(new Timestamp(scheduler.getStartTime()));
        instance.setInstanceid(scheduler.getInstanceid());
        instance.setSchedulerName(scheduler.getSchedulerName());
        instance.setConfigerid(scheduler.getConfigerid());
        instance.setUpdaterid(scheduler.getUpdaterid());
        instance.setSubmiter(scheduler.getSubmiter());
        List<String> workFlowNames = scheduler.getWorkFlowNames();
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < workFlowNames.size(); i++) {
            sb.append(workFlowNames.get(i));
            if (i < workFlowNames.size() - 1) {
                sb.append(",");
            }
        }
        instance.setFlowName(sb.toString());
        return instance;
    }

    /**
     * 转换信息WorkSchedulerInstance--->WorkScheduler
     */
    public WorkScheduler decodeWorkSchedulerInstance(WorkSchedulerInstance instance) {
        WorkScheduler scheduler = new WorkScheduler();

        scheduler.setConfig(JSONObject.parseObject(instance.getConfigParamJSON(), WorkConfig.class));
        scheduler.setCronStr(instance.getCronStr());
        scheduler.setEndTime(instance.getEndTime().getTime());
        scheduler.setStartTime(instance.getStartTime().getTime());
        scheduler.setRunTime(instance.getRunTime().getTime());
        scheduler.setFlowName(instance.getFlowName());
        scheduler.setInstanceid(instance.getInstanceid());
        scheduler.setRunParam(JSONObject.parseObject(instance.getRunParamJSON(), WorkConfig.class));
        scheduler.setSchedulerName(instance.getSchedulerName());
        scheduler.setSubmiter(instance.getSubmiter());
        scheduler.setConfigerid(instance.getConfigerid());
        scheduler.setUpdaterid(instance.getUpdaterid());

        return scheduler;
    }

    public WorkScheduler parseWorkSchedulerJson(String workSchedulerJson) {
        WorkScheduler workScheduler = new WorkScheduler();
        JSONObject jsonObject = JSONObject.parseObject(workSchedulerJson);
        workScheduler.setCronStr(jsonObject.getString("cronStr"));
        workScheduler.setFlowName(jsonObject.getString("flowName"));
        workScheduler.setSchedulerName(jsonObject.getString("schedulerName"));
        workScheduler.setStartTime(jsonObject.getLong("startTime"));
        workScheduler.setArguments(jsonObject.getObject("arguments", new TypeReference<Map<String, Map<String, String>>>() {
        }));
        workScheduler.setEndTime(jsonObject.getLong("endTime"));
        workScheduler.setInstanceid(jsonObject.getString("instanceid"));
        workScheduler.setVersions(jsonObject.getLongValue("versions"));
        workScheduler.setDate(jsonObject.getDate("date"));
        return workScheduler;
    }
}
