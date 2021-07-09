package com.reanon.community.service;

import com.reanon.community.dao.MessageMapper;
import com.reanon.community.entity.Message;
import com.reanon.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 私信/系统通知相关
 * ----系统通知: 1
 * ----用户私信: userId
 *
 * @author reanon
 * @create 2021-07-04
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询当前用户的会话数量
     *
     * @param userId
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询某个会话所包含的私信列表
     *
     * @param conversationId
     * @param offset
     * @param limit
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询某个会话所包含的私信数量
     *
     * @param conversationId
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信的数量
     *
     * @param userId
     * @param conversationId
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 读取私信(将私信状态设置为已读)
     *
     * @param ids
     */
    public int readMessage(List<Integer> ids) {
        // 将私信状态设置为已读
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 添加一条私信
     *
     * @param message
     */
    public int addMessage(Message message) {
        // 转义 HTML 标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        // 过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    /**
     * 查询某个主题下最新的系统通知
     *
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询某个主题下包含的系统通知数量
     *
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询未读的系统通知数量
     *
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeUnReadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnReadCount(userId, topic);
    }

    /**
     * 查询某个主题所包含的通知列表
     *
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
