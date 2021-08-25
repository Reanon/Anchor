package com.reanon.community.controller;

import com.reanon.community.entity.*;
// import com.reanon.community.event.EventProducer;
// import com.reanon.community.service.CommentService;
import com.reanon.community.event.EventProducer;
import com.reanon.community.service.CommentService;
import com.reanon.community.service.DiscussPostService;
// import com.reanon.community.service.LikeService;
import com.reanon.community.service.LikeService;
import com.reanon.community.service.UserService;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.CommunityUtil;
import com.reanon.community.utils.HostHolder;
// import com.reanon.community.utils.RedisKeyUtil;
import com.reanon.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * 帖子
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;
    // 评论相关
    @Autowired
    private CommentService commentService;
    // 点赞相关
    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 网站域名
    @Value("${community.path.domain}")
    private String domain;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // // editorMd 图片上传地址
    // @Value("${community.path.editormdUploadPath}")
    // private String editormdUploadPath;

    /**
     * 进入帖子发布页
     *
     * @return
     */
    @GetMapping("/publish")
    public String getPublishPage() {
        return "/site/discuss-publish";
    }



    /**
     * 添加帖子（发帖）
     *
     * @param title   帖子标题
     * @param content 帖子内容
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        // 获取发布帖子的对象
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        // 添加帖子: 需要避免注入攻击和敏感词过滤
        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件，通过 Kafka 的消息队列将消息传给 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 贴子发布时，计算帖子初始分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 进入帖子详情页
     * 评论：给帖子的评论
     * 回复：给评论的评论
     *
     * @param discussPostId
     * @param model
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        // 内容反转义，不然 markDown 格式无法显示
        String content = HtmlUtils.htmlUnescape(discussPost.getContent());
        discussPost.setContent(content);
        model.addAttribute("post", discussPost);
        // 查询作者, 从数据库中查, 这里需要优化
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 当前登录用户的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());// 帖子下的评论数目

        // 「帖子的评论」列表
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        // 封装评论及其相关信息
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 存储帖子的评论
                Map<String, Object> commentVo = new HashMap<>();
                // 评论内容
                commentVo.put("comment", comment); // 评论
                // 发布评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 该评论点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 当前登录用户对该评论的点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                        hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);


                // 存储每个评论对应的回复(不做分页)
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 封装对评论的评论和评论的作者信息
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply); // 回复
                        replyVo.put("user", userService.findUserById(reply.getUserId())); // 发布该回复的作者
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId
                                ());
                        replyVo.put("target", target); // 该回复的目标用户
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        // 该回复的点赞数量
                        replyVo.put("likeCount", likeCount);
                        // 当前登录用户的点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                                hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVoList);

                // 每个评论对应的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    /**
     * 置顶帖子
     */
    @PostMapping("/top")
    @ResponseBody
    public String updateTop(int id, int type) {
        discussPostService.updateType(id, type);

        // 触发发帖事件，通过 Kafka 的消息队列将消息异步传给 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


    /**
     * 加精帖子
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器, 交给 kafka 来异步实现
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }


    /**
     * 删除帖子
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        // 触发删帖事件，通过消息队列更新 Elasticsearch 服务器
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
    // /**
    //  * markdown 图片上传
    //  *
    //  * @param file
    //  * @return
    //  */
    // @PostMapping("/uploadMdPic")
    // @ResponseBody
    // public String uploadMdPic(@RequestParam(value = "editormd-image-file", required = false) MultipartFile file) {
    //
    //     String url = null; // 图片访问地址
    //     try {
    //         // 获取上传文件的名称
    //         String trueFileName = file.getOriginalFilename();
    //         String suffix = trueFileName.substring(trueFileName.lastIndexOf("."));
    //         String fileName = CommunityUtil.generateUUID() + suffix;
    //
    //         // 图片存储路径
    //         File dest = new File(editormdUploadPath + "/" + fileName);
    //         if (!dest.getParentFile().exists()) {
    //             dest.getParentFile().mkdirs();
    //         }
    //
    //         // 保存图片到存储路径
    //         file.transferTo(dest);
    //
    //         // 图片访问地址
    //         url = domain + contextPath + "/editor-md-upload/" + fileName;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return CommunityUtil.getEditorMdJSONString(0, "上传失败", url);
    //     }
    //
    //     return CommunityUtil.getEditorMdJSONString(1, "上传成功", url);
    // }
}
