package com.reanon.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author reanon
 * @create 2021-07-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    private int id;
    // 消息发送发
    private int fromId;
    // 消息接收方
    private int toId;
    // 对话id
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
