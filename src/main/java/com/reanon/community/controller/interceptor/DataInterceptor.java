package com.reanon.community.controller.interceptor;

import com.reanon.community.entity.User;
import com.reanon.community.service.DataService;
import com.reanon.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Data 拦截器: 用以做网站统计
 *
 * @author reanon
 * @create 2021-07-22
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    // 获取当前活跃用户
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取 IP
        String ip = request.getRemoteHost();
        // 统计 UV
        dataService.recordUV(ip);

        // 统计 DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
