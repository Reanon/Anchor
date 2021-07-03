package com.reanon.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author reanon
 * @create 2021-07-03
 */
@Controller
@RequestMapping("/alpha")
public class AlphaController {

    /**
     * cookie测试
     * 返回 http://localhost:8080/anchor/alpha/setCookie 查看
     *
     * @param response
     * @return
     */
    @RequestMapping("/setCookie")
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("testCookieCode", "123123");
        cookie.setPath("/anchor/alpha");
        // 生存时间，负数为临时性，即只在浏览器内存中，关闭就失效；
        // 0表示立刻失效，可配合 add 方法删除cookie
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);
        return "set cookie";
    }

    /**
     * 获取浏览器中名为 testCookieCode的Cookie
     *
     * @param cookie 浏览器中的 Cookie
     * @return
     */
    @GetMapping("/getCookie")
    @ResponseBody
    public String getCookie(@CookieValue("testCookieCode") String cookie) {
        return cookie;
    }

    /**
     * Session同 Map一样，可以声明并自动注入
     * 存信息入 session
     *
     * @param session 会话
     * @return
     */
    @GetMapping("/setSession")
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id", 23);
        session.setAttribute("name", "alice");
        return session.toString();
    }

    /**
     * 获取 session
     * Session同 Map一样，可以声明并自动注入
     * @param session
     * @return
     */
    @RequestMapping("/getSession")
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }
}