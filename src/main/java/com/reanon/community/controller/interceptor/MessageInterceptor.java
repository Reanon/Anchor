package com.reanon.community.controller.interceptor;

import com.reanon.community.entity.User;
import com.reanon.community.service.MessageService;
import com.reanon.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理主页上当前用户的所有未读消息
 * 使用拦截器
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    /**
     * Controller之后模板之前被调用
     * 获取未读私信/系统通知的数量
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            // 私信未读数量
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            // 系统通知未读数量
            int noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
            // 所有未读消息数量
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
