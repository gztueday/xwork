package com.bigdata.xwork.dao.mapper;

import com.bigdata.xwork.dao.entity.Bundle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zouyi on 2018/3/9.
 */
@Mapper
public interface SchedulerConfigBundleMapper {

    void insert(Bundle bundle);

    List<Bundle> queryBundleConfig(@Param("schedulerName") String schedulerName);

    void delete(@Param("schedulerName") String schedulerName);

}
