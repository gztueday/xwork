package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.action.core.WorkRunStatus;
import com.bigdata.xwork.dao.entity.WorkFlowInstance;
import com.bigdata.xwork.query.WorkFlowInstanceQueryObject;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Mapper
public interface WorkFlowInstanceMapper {

    @Insert("insert into xwork.work_flow_instance(flow_name,status,run_param_json,config_param_json,work_flow_json,run_time,start_time,scheduler_id,instance_id,submiter_id,execute_ip,`delete`,father_instanceid,versions,running_params "
            + ") values(#{flowName},#{status},#{runParamJSON},#{configParamJSON},#{workFlowInfoJSON},#{runTime},#{startTime},#{schedulerid},#{instanceid},#{submiterid},#{executeIP},#{delete},#{fatherInstanceId},#{versions},#{runningParams})")
    void addFlowInstance(@Param("flowName") String flowName, @Param("status") WorkRunStatus status, @Param("runParamJSON") String runParamJSON
            , @Param("configParamJSON") String configParamJSON, @Param("workFlowInfoJSON") String workFlowInfoJSON
            , @Param("runTime") Timestamp runTime, @Param("startTime") Timestamp startTime
            , @Param("schedulerid") String schedulerid, @Param("submiterid") long submiterid
            , @Param("instanceid") String instanceid, @Param("executeIP") String executeIP
            , @Param("delete") boolean delete, @Param("fatherInstanceId") String fatherInstanceId
            , @Param("versions") int versions, @Param("runningParams") String runningParams);

    List<WorkFlowInstance> getFlowInstancesByStatus(@Param("status") WorkRunStatus status);

    int queryForCount(WorkFlowInstanceQueryObject qo);

    List<WorkFlowInstance> query(WorkFlowInstanceQueryObject qo);

    boolean delete(@Param("instanceid") String instanceid);

    @Update("update xwork.work_flow_instance set status=#{status},execute_ip=#{executeIP} where instance_id=#{instanceid} and status=#{distribute}")
    int lockFlowInstance(@Param("instanceid") String instanceid, @Param("status") WorkRunStatus status, @Param("distribute") WorkRunStatus distribute, @Param("executeIP") String executeIP);

    @Update("update xwork.work_flow_instance set run_param_json=#{runParamJson},status=#{status},end_time=#{endTime} where instance_id=#{instanceid}")
    void updateRunParamStatus(@Param("runParamJson") String runParamJson, @Param("instanceid") String instanceid, @Param("status") WorkRunStatus status, @Param("endTime") Timestamp endTime);

    @Update("update xwork.work_flow_instance set status=#{status},execute_ip=#{ip} where instance_id=#{instanceid} and status=#{pending}")
    int lockFlowInstanceForPending(@Param("instanceid") String instanceid, @Param("status") WorkRunStatus status, @Param("pending") WorkRunStatus pending, @Param("ip") String ip);

    @Update("update xwork.work_flow_instance set status=#{success},end_time=#{endTime} where instance_id=#{instanceid}")
    int updateWorkFlowStatus(@Param("instanceid") String instanceid, @Param("success") WorkRunStatus success, @Param("endTime") Timestamp endTime);

    @Update("update xwork.work_flow_instance set status=#{failed} where `delete`=0 and instance_id=#{instanceid}")
    void updateWorkFlowInstanceToFail(@Param("instanceid") String instanceid, @Param("failed") WorkRunStatus failed);

    List<WorkFlowInstance> selectByFatherInstanceId(@Param("instanceId") String instanceId);

    WorkFlowInstance selectWorkInstanceByid(@Param("instanceId") String instanceId);

    void updateWorkFlowInstance(WorkFlowInstance instance);

    WorkFlowInstance selectWorkInstance(@Param("fatherInstanceId") String fatherInstanceId, @Param("workFlowName") String workFlowName);

    void updateStatus(@Param("instanceid") String instanceid, @Param("status") WorkRunStatus status);

    WorkFlowInstance selectWorkInstanceWithVersions(@Param("fatherInstanceId") String fatherInstanceId, @Param("workFlowName") String workFlowName, @Param("versions") int version);

    void updateWorkFlowInstanceStatus(
            @Param("status") WorkRunStatus status,
            @Param("versions") int versions,
            @Param("schedulerid") String schedulerid,
            @Param("fatherInstanceId") String fatherInstanceId,
            @Param("startTime") Date startTime,
            @Param("runTime") Date runTime);

    void updateWorkFlowJson(@Param("fatherInstanceId") String fatherInstanceId, @Param("versions") int versions, @Param("schedulerid") String schedulerid, @Param("workFlowJson") String workFlowJson);

    String selectInstance(@Param("fatherInstanceId") String fatherInstanceId, @Param("workFlowName") String workFlowName);

    List<WorkFlowInstance> selectByFatherInstanceIdForRunningORPending(@Param("fatherId") String fatherId, @Param("pending") WorkRunStatus pending, @Param("running") WorkRunStatus running);

    void bundleSchedulerRelation(@Param("instanceId") String instanceId, @Param("workFlowInfo") String workFlowInfo);

    String queryWorkFlowInstanceIp(@Param("wfInstanceid")String wfInstanceid);
}
