package com.reanon.community.service;

import com.reanon.community.dao.CommentMapper;
import com.reanon.community.entity.Comment;
import com.reanon.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.reanon.community.utils.CommunityConstant.ENTITY_TYPE_POST;

/**
 * 评论相关的业务方法
 * @author reanon
 * @create 2021-07-04
 */
@Service
public class CommentService {
    // 评论Dao层方法
    @Autowired
    private CommentMapper commentMapper;
    // 敏感词过滤
    @Autowired
    private SensitiveFilter sensitiveFilter;
    // 帖子相关
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 根据 id 查询评论
     * @param id
     */
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }


    /**
     * 根据评论目标（类别、id）对评论进行分页查询
     * @param entityType 评论目标
     * @param entityId 评论 id
     * @param offset  偏移
     * @param limit 限制数量
     */
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }
    /**
     * 查询某一类别的评论的数量
     * @param entityType 评论类型
     * @param entityId 评论的id
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 分页查询某个用户的评论/回复列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentByUserId(int userId, int offset, int limit) {
        return commentMapper.selectCommentByUserId(userId, offset, limit);
    }

    /**
     * 查询某个用户的评论/回复数量
     * @param userId
     * @return
     */
    public int findCommentCountByUserId(int userId) {
        return commentMapper.selectCommentCountByUserId(userId);
    }

    /**
     * 添加评论, 需要事务管理
     * 使用声明式事务
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // Html 标签转义
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 添加评论
        int rows = commentMapper.insertComment(comment);

        // 更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }
}
