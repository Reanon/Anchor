package com.reanon.community;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {
    // RedisTemplate
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    @DisplayName("测试 Redis 的string 类型操作")
    public void testStrings() {
        String redisKey = "test:count";
        // 操作 string 类型的数据
        redisTemplate.opsForValue().set(redisKey, 1);
        // 取数据
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
    }

    @Test
    @DisplayName("测试Redis的 hash 类型操作")
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "alice");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    @DisplayName("测试Redis的 list 类型操作")
    public void testLists() {
        String redisKey = "test:ids";
        // 左存
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        // 左弹出
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    @DisplayName("测试Redis的 set 类型操作")
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");
        // 操作集合
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    @DisplayName("测试Redis的 sorted_set 类型操作")
    public void testSortedSets() {
        String redisKey = "test:students";
        // 添加有序集合
        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);
        // 操作 有序集合
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "八戒"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    public void testKeys() {
        // 删除 key
        redisTemplate.delete("test:user");
        // 判断 key 是否存在
        System.out.println(redisTemplate.hasKey("test:user"));
        // 设置 key 的过期时间
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // 多次访问同一key,批量发送命令,节约网络开销
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        // 绑定 string 类型的数据
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // redis 的 编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                // 1、启用事务
                redisOperations.multi();

                // 操作 Redis
                redisOperations.opsForSet().add(redisKey, "alice");
                redisOperations.opsForSet().add(redisKey, "bob");
                redisOperations.opsForSet().add(redisKey, "candy");

                // 因此 redis 不要在事务内做查询, 是无效的
                // 这个结果的返回无论如何都为空(不管之前有没有数据)
                System.out.println(redisOperations.opsForSet().members("test:tx"));

                // 2、提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }
}
