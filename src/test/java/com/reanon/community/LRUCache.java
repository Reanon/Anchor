package com.reanon.community;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author reanon
 * @create 2021-07-17
 */
public class LRUCache {
    private static Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        int[] weight = {1, 3, 4};
        int[] value = {15, 20, 30};
        int bagSize = 4;
        completeBag(weight, value, bagSize);
    }

    private static void completeBag(int[] weight, int[] value, int bagSize) {
        // dp数组， dp[i] 表示容量 i 的背包装物品的最大价值
        int[] dp = new int[bagSize + 1];
        // 先遍历物品，在遍历背包
        for (int i = 0; i < weight.length; i++) {
            // 从前往后遍历
            for (int j = weight[i]; j <= bagSize; j++) {
                dp[j] = Math.max(dp[j], dp[j - weight[i]] + value[i]);
            }
        }
        // 打印dp数组
        Arrays.stream(dp).forEach(i -> System.out.print(i + " "));
    }
}
