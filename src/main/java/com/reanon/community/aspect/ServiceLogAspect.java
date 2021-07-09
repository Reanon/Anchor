package com.reanon.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一日志记录
 */
@Component
@Aspect
public class ServiceLogAspect {
    // 日志组件
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 定义切入点：任意返回值, service包的任意xxx类的任意参数的任意方法
    @Pointcut("execution(* com.reanon.community.service.*.*(..))")
    public void pointcut() {}

    /**
     * Aop 在切入点之前调用
     *
     * @param joinPoint 切入点方法
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[IP 地址], 在某个时间访问了 [com.reanon.community.service.xxx]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        // 获取 Ip 和时间
        String ip = request.getRemoteHost();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 获取切入点的类全限定名与方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s], 在[%s], 访问了[%s].", ip, time, target));
    }
}
