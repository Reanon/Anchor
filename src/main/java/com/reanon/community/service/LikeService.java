package com.reanon.community.service;

import com.reanon.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞相关
 */
@Service
public class LikeService {
    // 注入 RedisTemplate
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 实现点赞的业务方法
     *
     * @param userId       点赞的用户 id
     * @param entityType   点赞的实体类型
     * @param entityId     实体 id
     * @param entityUserId 被赞的帖子/评论的作者 id
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 开启事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 实体在 Redis 中的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

                // 实体作者在 Redis 中的 key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                // 判断用户是否已经点过赞了
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                // 1、开启事务
                redisOperations.multi();
                if (isMember) {
                    // 如果用户已经点过赞，点第二次则取消赞
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    // 将被赞作者的受赞数减一
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    // 该用户没有点赞，则给实体添加点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                // 2、提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某实体被点赞的数量
     *
     * @param entityType 实体类型
     * @param entityId  实体Id
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 计算该实体 key 中值的数量
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某个用户对某个实体的点赞状态（是否已赞）
     *
     * @param userId      点赞的用户
     * @param entityType  实体类型: 帖子 或 评论
     * @param entityId    实体 Id
     * @return 1:已赞，0:未赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 查询该用户是否点赞了当前帖子
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * 查询某个用户获得赞数量
     *
     * @param userId  用户 Id
     */
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}

