package com.reanon.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author reanon
 * @create 2021-07-22
 */
@Configuration
@EnableScheduling
@EnableAsync
// 需要这个配置类才能启动 Spring 定时任务
public class ThreadPoolConfig {
}
