package com.reanon.community.service;


import com.reanon.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 网站数据统计（UV / DAU）
 *
 * @author reanon
 * @create 2021-07-22
 */
@Service
public class DataService {
    // 注入Redis 组件
    @Autowired
    private RedisTemplate redisTemplate;
    // 设置日期格式
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 将指定的 IP 计入当天的 UV，独立访客
     *
     * @param ip 访客的 ip
     */
    public void recordUV(String ip) {
        // 将 PREFIX_UV + SPLIT + date 作为独立访客的 key
        // 以时间作为 key
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        // 存入 HyperLogLog
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    /**
     * 统计指定日期范围内的 UV
     *
     * @param start 开始日期
     * @param end  结束日期
     */
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的 key
        List<String> keyList = new ArrayList<>();
        // 实例化 Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        // 从 Redis 中逐天取出 key
        // 当 calendar 时间早于 end 就循环
        while (!calendar.getTime().after(end)) {
            // 取出这天的 key，存入 list
            String key = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1); // 加1天
        }

        // 合并这些天的 UV: 组成一个新的 key (区间 UV)
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(start), dateFormat.format(end));
        // 将 keyList 中的值合并，然后存入 redisKey 中
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计结果 —— 期间的访客数
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * 将指定的 IP 计入当天的 DAU
     *
     * @param userId 用户 ID
     */
    public void recordDAU(int userId) {
        // 获取当期时间的 key
        String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        // 将数据存入 Bitmap，以用户名作为索引
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    /**
     * 统计指定日期范围内的 DAU
     *
     * @param start  开始日期
     * @param end    结束日期
     */
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的 key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            // 将 DAU 的 Key 存入集合
            String key = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1); // 加1天
        }

        // 进行 OR 运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                // 将这一段时间的 DAU 数存入新的 key
                String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(start), dateFormat.format(end));
                // 传入: 操作符、 存入的key、计算的 keys
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                // 统计
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }
}
