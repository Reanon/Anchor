package com.reanon.community;

import com.reanon.community.dao.DiscussPostMapper;
import com.reanon.community.dao.LoginTicketMapper;
import com.reanon.community.dao.MessageMapper;
import com.reanon.community.dao.UserMapper;
import com.reanon.community.entity.DiscussPost;
import com.reanon.community.entity.LoginTicket;
import com.reanon.community.entity.Message;
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

    // 登陆凭证
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MessageMapper messageMapper;

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

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 5));   //5分钟

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectAndUpdateLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters() {
        // 查询用户111 的所有会话(只显示最新的私信)
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        messages.forEach(System.out::println);
        // 查询对话的数量
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
        // 查询会话 111_112 的所有私信
        messages = messageMapper.selectLetters("111_112", 0, 10);
        messages.forEach(System.out::println);
        // 查询会话 111_113 的私信数量
        count = messageMapper.selectLetterCount("111_113");
        System.out.println(count);
        // 用户 131 在 111_131 对话中的未读私信数量
        int unreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(unreadCount);
    }
}
