package com.reanon.community.dao;

import com.reanon.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author reanon
 * @create 2021-07-04
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据 id 查询评论
     */
    Comment selectCommentById(int id);

    /**
     * 根据评论目标(类别、id)对评论进行分页查询
     *
     * @param entityType 评论目标的类别
     * @param entityId   评论目标的 id
     * @param offset     每页的起始索引
     * @param limit      每页显示多少条数据
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);


    /**
     * 查询评论的数量
     *
     * @param entityType 评论的目标类型
     * @param entityId
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 添加评论
     *
     * @param comment
     */
    int insertComment(Comment comment);

    /**
     * 分页查询某个用户的评论/回复列表
     *
     * @param userId
     */
    List<Comment> selectCommentByUserId(int userId, int offset, int limit);

    /**
     * 查询某个用户的评论/回复数量
     *
     * @param userId
     */
    int selectCommentCountByUserId(int userId);
}
