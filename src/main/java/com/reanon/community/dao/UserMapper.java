package com.reanon.community.dao;

import com.reanon.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author reanon
 * @create 2021-07-02
 */
@Mapper
public interface UserMapper {
    /**
     * 根据 id 查询用户
     */
    User selectById(int id);

    /**
     * 根据 username 查询用户
     */
    User selectByName(String username);

    /**
     * 根据 email 查询用户
     */
    User selectByEmail(String email);

    /**
     * 插入用户（注册）
     */
    int insertUser(User user);

    /**
     * 修改用户状态
     *
     * @param status 0：未激活，1：已激活
     */
    int updateStatus(int id, int status);

    /**
     * 修改头像
     *
     * @param headerUrl 头像链接
     */
    int updateHeader(int id, String headerUrl);

    /**
     * 修改密码
     *
     * @param password 新密码
     */
    int updatePassword(int id, String password);
}
