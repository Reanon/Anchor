package com.reanon.community.service;

import com.reanon.community.dao.LoginTicketMapper;
import com.reanon.community.dao.UserMapper;
import com.reanon.community.entity.LoginTicket;
import com.reanon.community.entity.User;
import com.reanon.community.utils.CommunityUtil;
import com.reanon.community.utils.MailClient;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.reanon.community.utils.CommunityConstant.*;

/**
 * 用户相关
 *
 * @author reanon
 * @create 2021-07-02
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    // 用以发送邮件
    @Autowired
    private MailClient mailClient;

    // 用以发送 Html 的邮件
    @Autowired
    private TemplateEngine templateEngine;

    // 网站域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // Redis 优化登录凭证
    @Autowired
    private RedisTemplate redisTemplate;

    // 被废弃
    // @Autowired
    // private LoginTicketMapper loginTicketMapper;

    /**
     * 根据 Id 查询用户
     */
    public User findUserById(int id) {
        // 原: 直接从 MySQl 中查询
        // return userMapper.selectById(id);
        // 先从 Redis 中查询，如果不存在则先存入 Redis
        User user = getCache(id);
        if (user == null) {
            initCache(id);
        }
        return user;
    }


    /**
     * 根据 username 查询用户
     */
    public User findUserByName(String username) {

        return userMapper.selectByName(username);
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return Map<String, Object> 返回错误提示消息，如果返回的 map 为空，则说明注册成功
     */
    public Map<String, Object> register(User user) {
        // 用以保存错误信息
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        // 验证账号是否已存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        // 验证邮箱是否已存在
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        // 注册用户
        // 1、生成随机的 salt
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        // 2、对密码进行加盐加密
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 默认普通用户
        // 默认未激活
        user.setType(0);
        user.setStatus(0);
        // 3、生成激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 4、随机头像, 用户登录后可以自行修改
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 5、设置注册时间
        user.setCreateTime(new Date());
        // 插入数据
        userMapper.insertUser(user);

        // 给注册用户发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/anchor/activation/用户id/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活 Anchor 账号", content);
        return map;
    }

    /**
     * 激活用户
     *
     * @param userId 用户 id
     * @param code   激活码
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 用户已激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 修改用户状态为已激活
            userMapper.updateStatus(userId, 1);
            // 用户信息变更，清除 Redis 缓存中的旧数据
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 用户登录（为用户创建凭证）
     *
     * @param expiredSeconds 多少秒后凭证过期
     * @return Map<String, Object> 返回错误提示消息以及 ticket(凭证)
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        // 验证账号状态
        if (user.getStatus() == 0) {
            // 账号未激活
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        // 验证密码
        // 明文密码+盐再进行加密
        password = CommunityUtil.md5(password + user.getSalt());
        // 密码进行比对
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

        // 1、用户名和密码均正确，则为该用户生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        // 随机凭证: 随机字符串
        loginTicket.setTicket(CommunityUtil.generateUUID());
        // 设置凭证状态为有效（当用户登出的时候，设置凭证状态为无效）
        loginTicket.setStatus(0);
        // 设置凭证到期时间
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // 将登录凭证存入MySQl 数据库
        // loginTicketMapper.insertLoginTicket(loginTicket);

        // 2、优化: 将登录凭证存入 Redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 用户退出（将凭证状态设为无效）
     *
     * @param ticket
     */
    public void logout(String ticket) {
        // 设为登录无效
        // loginTicketMapper.updateStatus(ticket, 1);

        // 优化: 修改（先删除再插入）对应用户在 redis 中的凭证状态
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        // 设置登录凭证为无效
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    /**
     * 根据 ticket 查询 LoginTicket 信息
     *
     * @param ticket 登录凭证
     */
    public LoginTicket findLoginTicket(String ticket) {
        // 从 MySQL 中查询ticket
        // return loginTicketMapper.selectByTicket(ticket);

        // 优化: 从 Redis从查询登录凭证
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }


    /**
     * 修改用户头像
     *
     * @param userId
     * @param headUrl
     */
    public int updateHeader(int userId, String headUrl) {
        int rows = userMapper.updateHeader(userId, headUrl);
        // 用户信息变更，清除缓存中的旧数据
        clearCache(userId);
        return rows;
    }

    /**
     * 修改用户密码（对新密码加盐加密存入数据库）
     *
     * @param userId
     * @param newPassword 新密码
     * @return
     */
    public int updatePassword(int userId, String newPassword) {
        User user = userMapper.selectById(userId);
        // 重新加盐加密
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        // 用户信息变更，清除缓存中的旧数据
        clearCache(userId);
        return userMapper.updatePassword(userId, newPassword);
    }


    /**
     * 优先从缓存中取值
     *
     * @param userId
     */
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 缓存中没有该用户信息时，则将其存入缓存
     *
     * @param userId
     */
    private User initCache(int userId) {
        // 从MySQL 中查询到用户
        User user = userMapper.selectById(userId);
        // 存入 Redis 并设置过期时间
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 用户信息变更时清除对应缓存数据
     *
     * @param userId
     */
    private void clearCache(int userId) {
        // 清理缓存
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    /**
     * 获取某个用户的权限, 认证信息我们仍要想办法存到 SecurityContext里
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                // 返回用户类型, 也即权限
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
