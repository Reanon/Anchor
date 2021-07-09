package com.reanon.community.controller;

import com.reanon.community.entity.Comment;
import com.reanon.community.entity.DiscussPost;
import com.reanon.community.service.CommentService;
import com.reanon.community.service.DiscussPostService;
import com.reanon.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

import static com.reanon.community.utils.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.reanon.community.utils.CommunityConstant.ENTITY_TYPE_POST;

/**
 * 评论/回复
 * @author reanon
 * @create 2021-07-04
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    // 为了获取当前用户
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    // @Autowired
    // private EventProducer eventProducer;
    //
    // @Autowired
    // private RedisTemplate redisTemplate;
    /**
     * 添加评论
     * @param discussPostId 帖子的ID
     * @param comment 评论
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件（系统通知）
        // Event event = new Event()
        //         .setTopic(TOPIC_COMMNET)
        //         .setUserId(hostHolder.getUser().getId())
        //         .setEntityType(comment.getEntityType())
        //         .setEntityId(comment.getEntityId())
        //         .setData("postId", discussPostId);
        // if (comment.getEntityType() == ENTITY_TYPE_POST) {
        //     DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
        //     // event.setEntityUserId(target.getUserId());
        // }
        // else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
        //     Comment target = commentService.findCommentById(comment.getEntityId());
        //     event.setEntityUserId(target.getUserId());
        // }
        // eventProducer.fireEvent(event);
        //
        // if (comment.getEntityType() == ENTITY_TYPE_POST) {
        //     // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
        //     event = new Event()
        //             .setTopic(TOPIC_PUBLISH)
        //             .setUserId(comment.getUserId())
        //             .setEntityType(ENTITY_TYPE_POST)
        //             .setEntityId(discussPostId);
        //     eventProducer.fireEvent(event);
        //
        //     // 计算帖子分数
        //     String redisKey = RedisKeyUtil.getPostScoreKey();
        //     redisTemplate.opsForSet().add(redisKey, discussPostId);
        // }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
