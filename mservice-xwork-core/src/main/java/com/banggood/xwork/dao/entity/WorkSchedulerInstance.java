package com.banggood.xwork.dao.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionRelation;
import com.banggood.xwork.action.core.WorkFlow;
import com.banggood.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.scheduler.core.DispatcherScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkSchedulerInstance extends AbtractInstance {

    /**
     * cron表达式
     */
    private String cronStr;
    /**
     * cron表达式转出来的每次执行的时间,默认是20次
     */
    private Date date;

    /**
     * 所调度的work action flow名称
     */
    private String flowName;

    /**
     * 调度名称
     */
    private String schedulerName;
    /**
     * 以scheduler实例的Instance+scheduler实例的version确定另外一个具体的scheduler实例
     */
    private Long version;

    //-----------------------------------------------------------------------------------
    /**
     * 文件输出流
     */
    private FileOutputStream outputStream = null;
    private String executeIp;

    private String paramsJson;
    private Map<String, Map<String, String>> paramsMap;
    private WorkRunStatus schedulerStatus;

    private String relationsJson;
    /**
     * Map<actionName,Map< >>
     */
    private Map<String, WorkActionRelation> relationsMap;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public WorkRunStatus getSchedulerStatus() {
        return schedulerStatus;
    }

    public void setSchedulerStatus(WorkRunStatus schedulerStatus) {
        this.schedulerStatus = schedulerStatus;
    }

    public FileOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(FileOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, WorkActionRelation> getRelationsMap() {
        return relationsMap;
    }

    public void setRelationsMap(Map<String, WorkActionRelation> relationsMap) {
        this.relationsMap = relationsMap;
    }

    public Map<String, Map<String, String>> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(Map<String, Map<String, String>> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public String getRelationsJson() {
        return relationsJson;
    }

    public void setRelationsJson(String relationsJson) {
        this.relationsJson = relationsJson;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getExecuteIp() {
        return executeIp;
    }

    public void setExecuteIp(String executeIp) {
        this.executeIp = executeIp;
    }

    public String getCronStr() {
        return cronStr;
    }

    public void setCronStr(String cronStr) {
        this.cronStr = cronStr;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "WorkSchedulerInstance{" +
                "instanceid=" + this.instanceid +
                ", cronStr='" + cronStr + '\'' +
                ", date=" + date +
                ", flowName='" + flowName + '\'' +
                ", schedulerName='" + schedulerName + '\'' +
                ", executeIp='" + executeIp + '\'' +
                '}';
    }

    private final static Logger logger = LoggerFactory.getLogger(WorkSchedulerInstance.class);


    public void parseRunParam(JSONArray parameters, WorkFlow workFlow) {

        Map<String, Map<String, String>> actionParam = parseRunParam(parameters);

        for (String actionName : actionParam.keySet()) {
            WorkActionBase action = workFlow.getWorkActionByName(actionName);
            Map<String, String> argumentMaps = actionParam.get(actionName);
            if (action != null) {
                action.putActionsArguments(argumentMaps);
            } else {
                logger.error(" 找不到当前的action: {}", actionName);
                throw new RuntimeException("找不到当前的action: " + actionName);
            }
            logger.info(" putActionsArguments: {} ", argumentMaps);
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

    public String createScheudlerInstanceId() {
        return new StringBuilder(50).append("scheduler_instance_")
                .append(this.schedulerName).append("_").append(this.instanceid)
                .append("_").append(this.flowName).append("_").append(UUID.randomUUID().toString())
                .append(System.currentTimeMillis()).toString();
    }
}
