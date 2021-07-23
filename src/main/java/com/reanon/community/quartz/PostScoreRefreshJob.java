package com.reanon.community.quartz;

import com.reanon.community.entity.DiscussPost;
import com.reanon.community.service.DiscussPostService;
import com.reanon.community.service.ElasticsearchService;
import com.reanon.community.service.LikeService;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 帖子分数计算刷新的任务
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {
    // 统一日志记录
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    // Redis
    @Autowired
    private RedisTemplate redisTemplate;
    // 查询帖子
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    // 搜索引擎
    @Autowired
    private ElasticsearchService elasticsearchService;

    // Epoch 纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化 Epoch 纪元失败", e);
        }
    }
    // 实现定时任务
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 取出Redis 中与帖子分数相关的 key
        String redisKey = RedisKeyUtil.getPostScoreKey();

        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        // 逐个刷新分数
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕");
    }

    /**
     * 刷新帖子分数
     *
     * @param postId 帖子分数
     */
    private void refresh(int postId) {
        // 查出帖子 Id
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否加精
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 权重 + 发帖距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步贴子中的分数
        post.setScore(score);
        // 搜索引擎里也要更新数据
        elasticsearchService.saveDiscussPost(post);
    }
}
