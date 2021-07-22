package com.reanon.community;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

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

    // 统计20万个重复数据的独立总数.
    @Test
    @DisplayName("测试Redis的 HyperLogLog 类型操作")
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        // 统计去重之后数据的数量 - 99553
        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    // 将 3 组数据合并, 再统计合并后的重复数据的独立总数
    @Test
    @DisplayName("测试Redis的 HyperLogLog 类型合并操作")
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        // 构造三组数据
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }
        // 合并 3 组数据
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);
        // 统计数据的长度 -- 19833
        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统计一组数据的布尔值
    @Test
    @DisplayName("测试Redis的 BitMap 类型操作")
    public void testBitMap() {
        String redisKey = "test:bm:01";
        // 按位存数据
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询数据
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0)); // false
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1)); // true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2)); // false

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 统计该 key 中 1 的个数
                return connection.bitCount(redisKey.getBytes());
            }
        });
        // 结果为 3
        System.out.println(obj);
    }

    // 2、统计 3 组数据的布尔值, 并对这3组数据做OR运算.
    @DisplayName("测试Redis的 BitMap 类型 OR 操作")
    @Test
    public void testBitMapOperation() {
        // 构造三组数据，每组数据上位置上有重叠
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        // 对上面三个键做 OR 运算
        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 指定运算符
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                // 统计 redisKey 中为真的结果数
                return connection.bitCount(redisKey.getBytes());
            }
        });
        // 7
        System.out.println(obj);
        // 下面位置上都是 true
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }
}
