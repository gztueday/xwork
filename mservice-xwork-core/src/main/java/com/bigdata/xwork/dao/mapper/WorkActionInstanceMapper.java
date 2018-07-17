package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.action.core.WorkRunStatus;
import com.bigdata.xwork.dao.entity.WorkActionInstance;
import com.bigdata.xwork.dao.entity.WorkFlowInstance;
import com.bigdata.xwork.dao.entity.WorkSchedulerInstance;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface WorkActionInstanceMapper {

    @Insert("insert into xwork.work_action_instance(flow_name,action_name,scheduler_id,out_put,run_param_json,config_param_json,status,run_time,start_time,end_time,condition_pass,clazz,instance_id,flow_instanceid)"
            + "values(#{flowName},#{actionName},#{schedulerid},#{outPutJSON},#{runParamJSON},#{configParamJSON},#{status},#{runTime},#{startTime},#{endTime},#{conditionPass},#{clazz},#{instanceid},#{flowInstanceid})")
    void addWorkActionInstance(@Param("flowName") String flowName, @Param("actionName") String actionName,
                               @Param("schedulerid") String schedulerid, @Param("outPutJSON") String outPutJSON,
                               @Param("runParamJSON") String runParamJSON, @Param("configParamJSON") String configParamJSON,
                               @Param("status") WorkRunStatus status, @Param("runTime") Timestamp Timestamp, @Param("startTime") Timestamp startTime,
                               @Param("endTime") Timestamp endTime, @Param("conditionPass") boolean conditionPass,
                               @Param("clazz") String clazz, @Param("instanceid") String instanceid, @Param("flowInstanceid") String flowInstanceid);

    @Update("update xwork.work_action_instance set status=#{status},end_time=#{endTime},run_time=#{runTime},start_time=#{startTime},out_put=#{outPutJSON} where instance_id=#{instanceid}")
    void updateStatus(@Param("instanceid") String instanceid, @Param("status") WorkRunStatus status,
                      @Param("endTime") Timestamp endTime, @Param("runTime") Timestamp runTime, @Param("startTime") Timestamp startTime,
                      @Param("outPutJSON") String outPutJSON);


    @Select("select count(1) from xwork.work_action_instance where instance_id=#{instanceid}")
    boolean checkActionInstance(@Param("instanceid") String instanceid);

    WorkSchedulerInstance queryActionInstancesForScheduler(String schedulerInstanceId);

    List<WorkActionInstance> queryActionInstancesForInstanceId(String instanceid);

    WorkFlowInstance queryActionInstancesForWorkFlowInstanceId(String workFlowInstanceId);

    List<WorkSchedulerInstance> queryAll();

    WorkActionInstance queryActionByWFidAndActionName(@Param("wfInstanceid") String wfInstanceid, @Param("actionName") String actionName);

    WorkActionInstance queryByActionId(@Param("id") String id);
}
