package com.reanon.community.service;

import com.reanon.community.dao.DiscussPostMapper;
import com.reanon.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子相关
 *
 * @author reanon
 * @create 2021-07-02
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 分页查询讨论帖信息
     *
     * @param userId    当传入的 userId = 0 时查找所有用户的帖子
     *                  当传入的 userId != 0 时，查找该指定用户的帖子
     * @param offset    每页的起始索引
     * @param limit     每页显示多少条数据
     * @param orderMode 排行模式(若传入 1, 则按照热度来排序)
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {

        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 查询讨论贴的个数
     *
     * @param userId 当传入的 userId = 0 时计算所有用户的帖子总数
     *               当传入的 userId ！= 0 时计算该指定用户的帖子总数
     */
    public int findDiscussPostRows(int userId) {

        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
