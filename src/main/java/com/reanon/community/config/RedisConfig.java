package com.reanon.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {
    /**
     * 构造RedisTemplate
     * @param factory 是已经被实例化的bean，可以通过参数导入
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 实例化RedisTemplate
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置 key 的序列化的方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置 value 的序列化的方式
        template.setValueSerializer(RedisSerializer.json());
        // 设置 hash 的 key 的序列化的方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置 hash 的 value 的序列化的方式
        template.setHashValueSerializer(RedisSerializer.json());
        // 使上面设置的生效
        template.afterPropertiesSet();
        return template;
    }
}
