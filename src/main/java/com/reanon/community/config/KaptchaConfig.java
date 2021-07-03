package com.reanon.community.config;


import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;

import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Kaptcha 配置类（验证码）
 *
 * @author reanon
 * @create 2021-07-03
 */
@Configuration
public class KaptchaConfig {
    /**
     * 注入 kaptcha
     * Kaptcha 核心接口Producer, 其默认实现类DefaultKaptcha
     */
    @Bean
    public Producer kaptchaProducer() {
        // 设置配置文件
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 随机生成字符的范围
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 生成几个字符
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 添加噪声
        properties.setProperty("kaptcha.textproducer.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        // 生成 DefaultKaptcha
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
