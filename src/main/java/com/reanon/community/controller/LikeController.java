package com.reanon.community.controller;

// import com.reanon.community.entity.Event;

import com.reanon.community.entity.Event;
import com.reanon.community.entity.User;
// import com.reanon.community.event.EventProducer;
import com.reanon.community.event.EventProducer;
import com.reanon.community.service.LikeService;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.CommunityUtil;
import com.reanon.community.utils.HostHolder;
import com.reanon.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;


    /**
     * 点赞
     * 异步请求
     *
     * @param entityType   实体类型
     * @param entityId     实体 Id
     * @param entityUserId 赞的帖子/评论的作者 id
     * @param postId       帖子的 id (点赞了哪个帖子，点赞的评论属于哪个帖子，点赞的回复属于哪个帖子)
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 获取当前用户
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 将数据传输给页面
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件(系统通知), 取消点赞不通知
        if (likeStatus == 1) {
            // 点赞事件
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
