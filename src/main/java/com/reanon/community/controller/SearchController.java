package com.reanon.community.controller;

import com.reanon.community.entity.DiscussPost;
import com.reanon.community.entity.Page;
import com.reanon.community.service.ElasticsearchService;
import com.reanon.community.service.LikeService;
import com.reanon.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reanon.community.utils.CommunityConstant.ENTITY_TYPE_POST;

/**
 * 搜索相关表现层
 * @author reanon
 * @create 2021-07-11
 */
@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 搜索
     * search?keyword=xxx
     *
     * @param keyword 关键词
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子: 从 elasticsearch 服务器中进行搜索
        Map<String, Object> result = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1,
                page.getLimit());
        List<DiscussPost> discussPostList = (List<DiscussPost>) result.get("discussPosts");

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post : discussPostList) {
            Map<String, Object> map = new HashMap<>();
            // 帖子
            map.put("post", post);
            // 作者
            map.put("user", userService.findUserById(post.getUserId()));
            // 点赞数量
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 设置分页
        page.setPath("/search?keyword=" + keyword);
        page.setRows(((Long) result.get("totalCount")).intValue());

        return "/site/search";
    }
}
