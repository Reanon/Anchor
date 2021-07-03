package com.reanon.community.utils;

import com.reanon.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 使用多线程来持有用户信息, 用于代替 session 对象
 */
@Component
public class HostHolder {
    // 创建 ThreadLocal 对象
    private ThreadLocal<User> users = new ThreadLocal<>();
    // 存储 User
    public void setUser(User user) {
        users.set(user);
    }
    // 获取 User
    public User getUser() {
        return users.get();
    }
    // 清理
    public void clear() {
        users.remove();
    }
}
