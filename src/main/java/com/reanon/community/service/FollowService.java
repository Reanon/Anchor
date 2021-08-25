package com.reanon.community.service;

import com.reanon.community.entity.User;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 关注相关
 */
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     *
     * @param userId     主动关注的人
     * @param entityType 被关注的实体类型
     * @param entityId   被关注的 Id
     */
    public void follow(int userId, int entityType, int entityId) {
        // 事务管理
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 生成 Redis 的 key
                // 用户关注实体的key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 被关注的实体的 Key: 实体的类型 和 Id
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 1、开启事务管理
                redisOperations.multi();
                // 插入数据: 插入到 zset 中, 以当前时间作为 score
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                // 2、提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * 取消关注
     *
     * @param userId     主动关注的用户
     * @param entityType 被关注的实体类型
     * @param entityId   被关注的 Id
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 生成 Redis 的 key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 开启事务管理
                redisOperations.multi();

                // 删除数据
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);

                // 提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某个用户关注的实体的数量
     *
     * @param userId     用户 id
     * @param entityType 实体类型
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某个实体的粉丝数量
     *
     * @param entityType  实体类型
     * @param entityId    实体 Id
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 判断当前用户是否已关注该实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 分页查询某个用户关注的人（偷个懒，此处没有做对其他实体的关注）
     *
     * @param userId  用户 Id
     * @param offset  分页
     * @param limit   每页显示条数
     * @return
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        // 查询关注的人
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 倒序查询
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            // map 存单个用户的信息
            Map<String, Object> map = new HashMap<>();

            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }

        return list;
    }

    /**
     * 分页查询某个用户的粉丝
     * （此处没有做对其他实体的粉丝）
     *
     * @param userId  用户 Id
     * @param offset  分页
     * @param limit   每页显示条数
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        // 结果封装为一个集合
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();

            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }
        return list;
    }
}
