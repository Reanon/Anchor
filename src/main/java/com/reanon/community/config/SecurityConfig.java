package com.reanon.community.config;

import com.reanon.community.utils.CommunityConstant;
import com.reanon.community.utils.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    /**
     * 忽略对静态资源的拦截
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    /**
     * 授权
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                    "/user/setting",// 用户设置
                        "/user/uploadOss",      // 上传头像
                        "/discuss/publish",     // 进入帖子发布页
                        "/comment/add/**",      // 添加评论等
                        "/letter/**",           // 私信
                        "/notice/**",           // 通知
                        "/like",                // 点赞
                        "/follow",              // 关注
                        "/unfollow"             // 取关
                        // "/discuss/add",         // 发帖
                )
                // 拥有以下任意权限就可以访问: 用户、管理员、版主
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                // 给这些设置权限
                .antMatchers(
                    "/discuss/top", // 置顶
                        "/discuss/wonderful"    // 加精
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete", // 删除帖子
                        "/data/**"                     // 查看网站统计数据
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN     // 管理员
                )
                // 其他的任意用户都可以访问
                .anyRequest().permitAll()
                // 配置 csrf 关闭
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                // 1、未登录时的处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        // XMLHttpRequest 表示异步请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            // 异步请求要求返回XML, 这里手动声明返回Json
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录"));
                        }
                        else {
                            // 普通请求, 则直接重定向到登录页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                // 2、权限不够时的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            // 异步请求, 返回 Json
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问该功能的权限"));
                        }
                        else {
                            // 普通请求
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // Security 底层会默认拦截 /logout 请求，进行退出处理
        // 此处赋予它一个根本不存在的退出路径, 使得程序能够执行到我们自己编写的退出代码
        http.logout().logoutUrl("/securityLogout");

        http.headers().frameOptions().sameOrigin();
    }
    // 认证环节使用自己的代码 LoginController, 绕过 Spring Security, 但是认证信息仍要想办法存到SecurityContext里
}
