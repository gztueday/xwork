package com.bigdata.xwork.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.bigdata.xwork.dao.entity.UserInfo;

@Mapper
public interface UserInfoMapper {

    @Select("select user_name as userName,user_id  as userid,group_id as groupid,create_time as createTime from xwork.user_info where user_name=#{userName}")
    UserInfo getUserInfoByName(@Param("userName") String userName);

    @Select("select user_name as userName,user_id  as userid,group_id as groupid,create_time as createTime,group_name as groupName from xwork.user_info where user_id=#{userid}")
    UserInfo getUserInfoByID(@Param("userid") long userid);

}
