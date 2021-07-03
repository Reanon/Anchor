package com.reanon.community.controller.interceptor;

import com.reanon.community.annotation.LoginRequired;
import com.reanon.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 在登陆之前拦截 @LoginRequired 的方法
 * @author reanon
 * @create 2021-07-03
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    // 判断当前是否登录
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断拦截目标是否为一个方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取拦截到的方法对象
            Method method = handlerMethod.getMethod();
            // 获取方法上的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 未登录则直接跳转到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
