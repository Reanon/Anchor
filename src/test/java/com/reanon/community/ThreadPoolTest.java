package com.reanon.community;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池测试
 *
 * @author reanon
 * @create 2021-07-22
 */
@SpringBootTest
public class ThreadPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    // JDK 普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK 可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring 普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // Spring 可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    // @Autowired
    // private AlphaService alphaService;


    // 主线程结束后会等待其他线程结束进程才退出
    private void sleep(long m) {
        try {
            // 等待一段时间
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("测试 JDK 普通线程池")
    public void testExecutorService() throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                sleep(1000);
                logger.debug("hello executorService");
            }
        };
        // 执行多次
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
        sleep(2000);
    }

    @Test
    @DisplayName(" 测试 ScheduledExecutorService 可执行定时任务的线程池")
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello scheduledExecutorService");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 5000, 1000, TimeUnit.MILLISECONDS);
        sleep(20000);
    }

    @Test
    @DisplayName("测试 Spring  普通线程池")
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello threadPoolTaskExecutor");
            }
        };
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
        sleep(2000);
    }

    @Test
    @DisplayName("测试 Spring  定时任务线程池")
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello threadPoolTaskScheduler");
            }
        };
        Date date = new Date(System.currentTimeMillis() + 5000);
        // 需要传入时间
        threadPoolTaskScheduler.scheduleAtFixedRate(task, date, 1000);
        sleep(20000);
    }

    // Spring 支持注解的方式，见 AlphaService
    @Test
    @DisplayName("异步执行多线程方法")
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            // alphaService.execute1();
        }
        sleep(2000);
    }

    // Spring 支持注解的方式，见 AlphaService
    @Test
    @DisplayName("Spring 定时任务线程池，简化方式")
    public void testThreadPoolTaskSchedulerSimple() {
        sleep(10000);
    }
}
