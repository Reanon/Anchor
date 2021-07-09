package com.reanon.community;

import com.reanon.community.utils.SensitiveFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author reanon
 * @create 2021-07-04
 */

@SpringBootTest
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    @DisplayName("测试敏感词过滤")
    public void testSensitiveFilter() {
        String text1 = "这里可以🎋赌博、可以🎍嫖🎍娼、可以吸毒，哈哈！";
        // 这里可以🎋***、可以🎍***、可以***，哈哈！
        System.out.println(sensitiveFilter.filter(text1));

        String text2 = "这里可以☆赌☆☆博、可以☆☆嫖☆娼☆☆、可以☆吸☆☆毒，哈哈！";
        // 这里可以☆***、可以☆☆***☆☆、可以☆***，哈哈！
        System.out.println(sensitiveFilter.filter(text2));
    }
}
