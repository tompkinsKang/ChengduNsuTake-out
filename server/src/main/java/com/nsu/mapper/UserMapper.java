package com.nsu.mapper;
/*
  Date:2025/3/19
  Time:15:04
  @author llh 
 */

import com.nsu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    // 根据openid查询用户
    @Select("select * from sky_take_out.User where openid = #{openId}")
    User getByOpenId(String openId);

    // 插入用户
    void insert(User user);

    @Select("select * from sky_take_out.User where id = #{id}")
    User getById(Long id);

    Integer getUserByMap(Map map);
}

