package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.dao.entity.WorkFlowInfo;
import com.bigdata.xwork.query.WorkFlowConfigQueryObject;
import com.bigdata.xwork.query.WorkFlowNameQueryObject;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface WorkFlowInfoMapper {


    @Insert("insert into xwork.work_flow(flow_name,config_param_json,relations_json,actions_json,configer_id,share,last_update_time,dependName,emails,create_time,`delete`,requestObj,uuid,paramMap,description,subWorkFlow_json) "
            + " values(#{flowName},#{configParamJSON},#{relationsJSON},#{actionsJSON},#{configerid},#{share},#{lastUpdateTime},#{dependName},#{emails},#{createTime},#{delete},#{requestObj},#{uuid},#{runParamMap},#{description},#{subWorkFlowJson})")
    void addWorkActionFlow(@Param("flowName") String flowName, @Param("configParamJSON") String configParamJSON,
                           @Param("relationsJSON") String relationsJSON, @Param("actionsJSON") String actionsJSON,
                           @Param("configerid") long configerid, @Param("share") boolean share, @Param("lastUpdateTime") Timestamp lastUpdateTime,
                           @Param("dependName") String dependName, @Param("emails") String emails,
                           @Param("createTime") Timestamp createTime, @Param("delete") boolean delete, @Param("requestObj") String requestObj,
                           @Param("uuid") String uuid, @Param("runParamMap") String paramMap, @Param("description") String description,
                           @Param("subWorkFlowJson") String subWorkFlowJson);

    boolean deleteWorkFlowByName(@Param("flowName") String flowName);

    WorkFlowInfo getWorkActionFlowByName(@Param("flowName") String flowName);

    List<WorkFlowInfo> getWorkFlowAll(WorkFlowConfigQueryObject qo);

    int queryForCount(WorkFlowConfigQueryObject qo);

    List<WorkFlowInfo> queryByName(WorkFlowNameQueryObject qo);

    int queryByNameForCount(@Param("workFlowName") String workFlowName);

    void update(@Param("flowName") String flowName, @Param("configParamJSON") String configParamJSON,
                @Param("relationsJSON") String relationsJSON, @Param("actionsJSON") String actionsJSON,
                @Param("configerid") long configerid, @Param("share") boolean share, @Param("lastUpdateTime") Timestamp lastUpdateTime,
                @Param("dependName") String dependName, @Param("emails") String emails,
                @Param("createTime") Timestamp createTime, @Param("delete") boolean delete, @Param("requestObj") String requestObj,
                @Param("uuid") String uuid);

    WorkFlowInfo getWorkFlowForUUID(@Param("uuid") String uuid);

    int getWorkFlowForCount(@Param("flowName") String flowName);

    void realDeByName(@Param("flowName") String flowName);

    List<WorkFlowInfo> queryWorkFlowByName(@Param("workFlowName") String workFlowName);
}
