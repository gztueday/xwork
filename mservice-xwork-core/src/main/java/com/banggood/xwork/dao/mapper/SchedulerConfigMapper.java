package com.banggood.xwork.dao.mapper;

import com.banggood.xwork.query.AcceptSchedulerObject;
import com.banggood.xwork.query.SchedulerConfigQueryObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zouyi on 2018/3/8.
 */
@Mapper
public interface SchedulerConfigMapper {

    void insert(AcceptSchedulerObject acceptSchedulerObject);

    AcceptSchedulerObject query(@Param("schedulerName") String schedulerName);

    int queryCount(@Param("schedulerName") String schedulerName);

    void delete(@Param("schedulerName") String schedulerName);

    int queryForCount(SchedulerConfigQueryObject qo);

    List<AcceptSchedulerObject> querySchedulerConfig(SchedulerConfigQueryObject qo);

    boolean deleteSchedulerConfig(@Param("schedulerName") String schedulerName);
}
