package com.reanon.community;

import com.reanon.community.dao.DiscussPostMapper;
import com.reanon.community.dao.UserMapper;
import com.reanon.community.entity.DiscussPost;
import com.reanon.community.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 * @author reanon
 * @create 2021-07-02
 */
@SpringBootTest
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    @DisplayName("测试 Select 方法")
    public void testSelectUser() {
        User user1 = userMapper.selectById(150);
        System.out.println(user1);

        User user2 = userMapper.selectByName("alice");
        System.out.println(user2);

        User user3 = userMapper.selectByEmail("alice@qq.com");
        System.out.println(user3);
    }

    @Test
    @DisplayName("测试 Insert 方法")
    public void testInsertUser() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("123456");
        user.setSalt("random");
        user.setEmail("alice@qq.com");
        user.setHeaderUrl("http://www.reanon.com/101");
        user.setCreateTime(new Date());
        // 受影响的行数
        int row = userMapper.insertUser(user);
        System.out.println(row);
    }

    @Test
    @DisplayName("测试 Update 方法")
    public void testUpdate() {
        userMapper.updateStatus(150, 1);
        userMapper.updateHeader(150, "http://www.reanon.com/102");
        userMapper.updatePassword(150, "hello");
    }

    @Test
    @DisplayName("测试 DiscussPost  方法")
    public void testSelectPosts() {
        // 查询所有所有帖子
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
        discussPosts.forEach(System.out::println);

        System.out.println(discussPostMapper.selectDiscussPostRows(0));
    }
}
