package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.action.core.WorkRunStatus;
import com.bigdata.xwork.dao.entity.WorkSchedulerInstance;
import com.bigdata.xwork.query.SchedulerInstanceQueryObject;
import com.bigdata.xwork.query.SchedulerStatusInstanceQueryObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by zouyi on 2018/3/9.
 */
@Mapper
public interface SchedulerInstanceMapper {

    void insert(WorkSchedulerInstance instance);

    WorkSchedulerInstance querySchedulerByInstanceidAndVersion(@Param("remoteInstanceid") String remoteInstanceid,
                                                               @Param("version") long version);

    void updateRelations(@Param("remoteInstanceid") String remoteInstanceid, @Param("version") long version,
                         @Param("schedulerRelations") String schedulerRelations);

    WorkSchedulerInstance queryByWorkFlowInstanceId(@Param("wfinstanceid") String wfinstanceid);

    int queryForCount(SchedulerInstanceQueryObject qo);

    List<WorkSchedulerInstance> query(SchedulerInstanceQueryObject qo);

    List<WorkSchedulerInstance> queryByScheduler(@Param("schedulerName") String schedulerName);

    List<WorkSchedulerInstance> BundleDate(@Param("id") String id);

    void updateSchedulerStatus(@Param("instanceid") String instanceid, @Param("status") WorkRunStatus status);

    int queryDispatcherCount(@Param("instanceid") String instanceid);

    void insertWorkScheduler(@Param("instanceid") String instanceid, @Param("date") Date date, @Param("version") long version);

    void updateInstanceDate(@Param("instanceid") String instanceid, @Param("date") Date date);

    Date selectExecuteDate(@Param("instanceid") String instanceid);

    void updateWorkFlowRelationIntoSchedulerStatus(@Param("instanceid") String schedulerInstance,
                                                   @Param("workFlowInstance") String workFlowInstance,
                                                   @Param("version") long version, @Param("runTime") Date runTime);

    void updateSchedulerInstanceToFail(@Param("schedulerid") String schedulerid,
                                       @Param("instanceid") String instanceid,
                                       @Param("status") WorkRunStatus status,
                                       @Param("updateTime") Date date);

    List<WorkSchedulerInstance> queryInstanceId(SchedulerStatusInstanceQueryObject qo);

    String querySchedulerName(@Param("schedulerInstance") String schedulerInstance);

    int queryInstanceIdCount(SchedulerStatusInstanceQueryObject qo);

    WorkSchedulerInstance querySchedulerNameForInstanceId(@Param("schedulerInstance") String schedulerInstance);

    String queryWorkFlowName(@Param("schedulerInstance") String schedulerInstance);

    List<WorkSchedulerInstance> queryLatestRunningSchedulerInstance(@Param("schedulerInstanceid") String schedulerInstanceid);

    WorkSchedulerInstance queryDispatcherScheduler(@Param("schedulerInstanceid") String schedulerInstanceid);

    List<WorkSchedulerInstance> querySchedulerByRunning(@Param("schedulerInstanceid") String schedulerInstanceid);

    long queryMinVersionForDispatcher(@Param("schedulerInstanceId") String schedulerInstanceId);

    long queryMaxVersionForDispatcher(@Param("schedulerInstanceId") String schedulerInstanceId);

    int querySchedulerInstanceCount(@Param("remoteInstanceid") String remoteInstanceid);

    int queryCountForDispatcher(@Param("schedulerInstanceId") String schedulerInstanceId);

    Date selectExecuteMinDate(@Param("schedulerInstanceId") String schedulerInstanceId);

    void updateRelationForRightScheduler(@Param("instanceid") String instanceid, @Param("relationJson") String relationJson);

    void updateSchedulerStatusGroup(@Param("schedulerInstanceid") String schedulerInstanceid, @Param("status") WorkRunStatus status);

    String queryExecuteIpBySchedulerInstanceId(@Param("schedulerInstance") String schedulerInstance);
}
