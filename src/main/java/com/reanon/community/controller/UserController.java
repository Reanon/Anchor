package com.reanon.community.controller;

// import com.reanon.community.entity.Comment;

import com.reanon.community.annotation.LoginRequired;
import com.reanon.community.entity.DiscussPost;
import com.reanon.community.entity.Page;
import com.reanon.community.entity.User;
import com.reanon.community.service.*;
import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.CommunityUtil;
import com.reanon.community.utils.HostHolder;
// import com.qiniu.util.Auth;
// import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/**
 * 用户
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    // 消息日志
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    // 网站域名
    @Value("${community.path.domain}")
    private String domain;

    // 用户头像上传路径
    @Value("${community.path.upload}")
    private String uploadPath;

    // 项目名(访问路径)
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // @Value("${qiniu.key.access}")
    // private String accessKey;
    //
    // @Value("${qiniu.key.secret}")
    // private String secretKey;
    //
    // @Value("${qiniu.bucket.header.name}")
    // private String headerBucketName;
    //
    // @Value("${qiniu.bucket.header.url}")
    // private String headerBucketUrl;

    /**
     * 跳转至账号设置界面
     *
     * @return
     */
    @LoginRequired // 配置访问时间
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        // 生成上传文件的名称
        String fileName = CommunityUtil.generateUUID();
        model.addAttribute("fileName", fileName);

        // // 设置响应信息(qiniu 的规定写法)
        // StringMap policy = new StringMap();
        // policy.put("returnBody", CommunityUtil.getJSONString(0));
        // // 生成上传到 qiniu 的凭证(qiniu 的规定写法)
        // Auth auth = Auth.create(accessKey, secretKey);
        // String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        // model.addAttribute("uploadToken", uploadToken);

        return "/site/setting";
    }

    /**
     * 更新头像(本地，已废弃)
     *
     * @param headerImage MVC 框架提供的API
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping(path = "/upload")
    public String uploadHeaderLocal(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您未选择图片！");
            return "/site/setting";
        }
        // 获取原始文件名(包含文件后缀)
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));  // .png等后缀
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        // 生成随机访问 url
        filename = CommunityUtil.generateUUID() + suffix;

        // 上传头像
        File file = new File(uploadPath + "/" + filename);
        try {
            // 直接写入文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！", e);
        }

        // 更新用户的 headerUrl(web访问路径)
        // http://localhost:8080/anchor/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        // 重定向到首页
        return "redirect:/index";
    }

    /**
     * 访问本地头像（废弃）
     *
     * @param filename
     * @param response
     */
    @GetMapping(path = "/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器实际存放头像位置
        filename = uploadPath + "/" + filename;

        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        // 注意这里只能用传统的文件 IO 方法而不能用验证码的ImageIO.write(image,"png",os);
        // 因为第一个参数类型不满足
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }
    }


    // /**
    //  * 更新图像路径（将本地的图像路径更新为云服务器上的图像路径）
    //  * @param fileName
    //  * @return
    //  */
    // @PostMapping("/header/url")
    // @ResponseBody
    // public String updateHeaderUrl(String fileName) {
    //     if (StringUtils.isBlank(fileName)) {
    //         return CommunityUtil.getJSONString(1, "文件名不能为空");
    //     }
    //
    //     // 文件在云服务器上的的访问路径
    //     String url = headerBucketUrl + "/" + fileName;
    //     userService.updateHeader(hostHolder.getUser().getId(), url);
    //
    //     return CommunityUtil.getJSONString(0);
    //
    // }
    //
    // /**
    //  * 修改用户密码
    //  * @param oldPassword 原密码
    //  * @param newPassword 新密码
    //  * @param model
    //  * @return
    //  */
    // @PostMapping("/password")
    // public String updatePassword(String oldPassword, String newPassword, Model model) {
    //     // 验证原密码是否正确
    //     User user = hostHolder.getUser();
    //     String md5OldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
    //     if (!user.getPassword().equals(md5OldPassword)) {
    //         model.addAttribute("oldPasswordError", "原密码错误");
    //         return "/site/setting";
    //     }
    //
    //     // 判断新密码是否合法
    //     String md5NewPassword = CommunityUtil.md5(newPassword + user.getSalt());
    //     if (user.getPassword().equals(md5NewPassword)) {
    //         model.addAttribute("newPasswordError", "新密码和原密码相同");
    //         return "/site/setting";
    //     }
    //
    //     // 修改用户密码
    //     userService.updatePassword(user.getId(), newPassword);
    //
    //     return "redirect:/index";
    // }
    //

    /**
     * 进入个人主页
     *
     * @param userId 可以进入任意用户的个人主页
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user", user);
        // 获赞数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前登录用户是否已关注该用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("tab", "profile"); // 该字段用于指示标签栏高亮

        return "/site/profile";
    }
    //
    // /**
    //  * 进入我的帖子（查询某个用户的帖子列表）
    //  * @param userId
    //  * @param page
    //  * @param model
    //  * @return
    //  */
    // @GetMapping("/discuss/{userId}")
    // public String getMyDiscussPosts(@PathVariable("userId") int userId, Page page, Model model) {
    //     User user = userService.findUserById(userId);
    //     if (user == null) {
    //         throw new RuntimeException("该用户不存在");
    //     }
    //     model.addAttribute("user", user);
    //
    //     // 该用户的帖子总数
    //     int rows = discussPostService.findDiscussPostRows(userId);
    //     model.addAttribute("rows", rows);
    //
    //     page.setLimit(5);
    //     page.setPath("/user/discuss/" + userId);
    //     page.setRows(rows);
    //
    //     // 分页查询(按照最新查询)
    //     List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
    //     // 封装帖子和该帖子对应的用户信息
    //     List<Map<String, Object>> discussPosts = new ArrayList<>();
    //     if (list != null) {
    //         for (DiscussPost post : list) {
    //             Map<String, Object> map = new HashMap<>();
    //             map.put("post", post);
    //             long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
    //             map.put("likeCount", likeCount);
    //
    //             discussPosts.add(map);
    //         }
    //     }
    //     model.addAttribute("discussPosts", discussPosts);
    //     model.addAttribute("tab", "mypost"); // 该字段用于指示标签栏高亮
    //
    //     return "/site/my-post";
    // }
    //
    // /**
    //  * 进入我的评论/回复（查询某个用户的评论/回复列表）
    //  * @param userId
    //  * @param page
    //  * @param model
    //  * @return
    //  */
    // @GetMapping("/comment/{userId}")
    // public String getMyComments(@PathVariable("userId") int userId, Page page, Model model) {
    //     User user = userService.findUserById(userId);
    //     if (user == null) {
    //         throw new RuntimeException("该用户不存在");
    //     }
    //     model.addAttribute("user", user);
    //
    //     // 该用户的评论/回复总数
    //     int commentCounts = commentService.findCommentCountByUserId(userId);
    //     model.addAttribute("commentCounts", commentCounts);
    //
    //     page.setLimit(5);
    //     page.setPath("/user/comment/" + userId);
    //     page.setRows(commentCounts);
    //
    //     // 分页查询
    //     List<Comment> list = commentService.findCommentByUserId(userId, page.getOffset(), page.getLimit());
    //     // 封装评论和该评论对应的帖子信息
    //     List<Map<String, Object>> comments = new ArrayList<>();
    //     if (list != null) {
    //         for (Comment comment : list) {
    //             Map<String, Object> map = new HashMap<>();
    //             map.put("comment", comment);
    //             // 显示评论/回复对应的文章信息
    //             if (comment.getEntityType() == ENTITY_TYPE_POST) {
    //                 // 如果是对帖子的评论，则直接查询 target_id 即可
    //                 DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
    //                 map.put("post", post);
    //             }
    //             else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
    //                 // 如过是对评论的回复，则先根据该回复的 target_id 查询评论的 id, 再根据该评论的 target_id 查询帖子的 id
    //                 Comment targetComment = commentService.findCommentById(comment.getEntityId());
    //                 DiscussPost post = discussPostService.findDiscussPostById(targetComment.getEntityId());
    //                 map.put("post", post);
    //             }
    //
    //             comments.add(map);
    //         }
    //     }
    //     model.addAttribute("comments", comments);
    //     model.addAttribute("tab", "myreply"); // 该字段用于指示标签栏高亮
    //
    //     return "/site/my-reply";
    //
    // }

}
