package com.reanon.community.controller.advice;

import com.reanon.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 处理服务端异常（500）
 */
// 扫描带有 @Controller 的组件
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    // 日志组件
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // 异常处理
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 日志里记录异常
        logger.error("服务器发生异常：" + e.getMessage());
        // 遍历异常栈的信息
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        // 区分异步请求和普通请求
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求(希望返回的是 JSON 数据)
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        }
        else {
            // 普通请求(希望返回的是一个网页)
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
