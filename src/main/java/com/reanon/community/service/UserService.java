package com.reanon.community.service;

import com.reanon.community.dao.UserMapper;
import com.reanon.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户相关
 *
 * @author reanon
 * @create 2021-07-02
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据 Id 查询用户
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 根据 username 查询用户
     */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}
