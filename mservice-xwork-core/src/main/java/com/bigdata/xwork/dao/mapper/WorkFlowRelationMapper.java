package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.dao.entity.WorkFlowInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by zouyi on 2018/1/28.
 */
@Mapper
public interface WorkFlowRelationMapper {

    WorkFlowInfo getWorkFlowByName(@Param("flowName") String workFlowName, @Param("fatherName") String fatherName);

    int updateRelationJson(@Param("workActionRelation") String workActionRelation, @Param("flowName") String flowName, @Param("fatherName") String fatherName);

    @Insert("insert into xwork.work_flow_relations(flow_name,config_param_json,relations_json,actions_json,configer_id,share,last_update_time,dependName,emails,create_time,`delete`,requestObj,uuid,flow_relation) "
            + " values(#{flowName},#{configParamJSON},#{relationsJSON},#{actionsJSON},#{configerid},#{share},#{lastUpdateTime},#{dependName},#{emails},#{createTime},#{delete},#{requestObj},#{uuid},#{flowRelation})")
    void insertRelation(@Param("flowName") String flowName, @Param("configParamJSON") String configParamJSON,
                        @Param("relationsJSON") String relationsJSON, @Param("actionsJSON") String actionsJSON,
                        @Param("configerid") long configerid, @Param("share") boolean share, @Param("lastUpdateTime") Timestamp lastUpdateTime,
                        @Param("dependName") String dependName, @Param("emails") String emails,
                        @Param("createTime") Timestamp createTime, @Param("delete") boolean delete, @Param("requestObj") String requestObj,
                        @Param("uuid") String uuid, @Param("flowRelation") String flowRelation);

    List<WorkFlowInfo> selectByRelation(@Param("fatherName")String fatherName);

    void deleteByRelation(@Param("flowRelation")String flowRelation,@Param("flowName") String flowName);
}