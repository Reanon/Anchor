package com.reanon.community.config;

import com.reanon.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Spring Quartz 配置类，用于将数据存入数据库，以后直接从数据库中调用数据
 * @author reanon
 * @create 2021-07-22
 */
@Configuration
public class QuartzConfig {
    // BeanFactory是 Spring 顶层容器
    // FactoryBean 简化 bean 的实例化过程
    // 1.Spring 通过 FactoryBean 封装 Bean 的实例化过程
    // 2.将 FactoryBean 装配到容器里
    // 3.将 FactoryBean 注入给其他 Bean
    // 4.该 Bean 得到的是 FactoryBean 管理的实例
    /**
     * 刷新帖子分数的任务
     * 配置 JobDetail
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        // 实例化对象
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 设置实例类型
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        // 设置 Job 名称
        factoryBean.setName("postScoreRefreshJob");
        // 设置组
        factoryBean.setGroup("communityJobGroup");
        // 声明任务是否长久保存
        factoryBean.setDurability(true);
        // 任务是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /**
     * 刷新帖子分数触发器
     * 配置 Trigger（SimpleTriggerFactoryBean, CronTriggerFactoryBean)
     */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        // 实例化 Trigger
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 传入 JobDetail
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        // 设置名称
        factoryBean.setName("postScoreRefreshTrigger");
        // 设置组名
        factoryBean.setGroup("communityTriggerGroup");
        // 设置刷新时间: 5分钟刷新一次
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        // Trigger 底层需要存储 Job 一些状态, 这里采用默认类型来存
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
