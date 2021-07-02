package com.reanon.community.controller;

import com.reanon.community.entity.DiscussPost;
import com.reanon.community.entity.Page;
import com.reanon.community.entity.User;
import com.reanon.community.service.DiscussPostService;
import com.reanon.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页
 *
 * @author reanon
 * @create 2021-07-02
 */
@Controller
public class IndexController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    /**
     * 转发进入首页
     */
    @GetMapping("/")
    public String root() {
        return "forward:/index";
    }


    /**
     * 进入首页
     * 方法调用前, SpringMVC会自动实例化Model和 Page,并将 Page 注入Model
     *  所以,在 thymeleaf 中可以直接访问 Page 对象中的数据
     * @param orderMode 默认是 0（最新）
     */
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {

        // 获取总页数
        page.setRows(discussPostService.findDiscussPostRows(0));
        // page.setPath("/index?orderMode=" + orderMode);
        page.setPath("/index");

        // 分页查询
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);

        // 封装帖子和该帖子对应的用户信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                // 贴子
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                // 帖子对应的发布者
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        // model 默认放入 Request 域中
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }

}
