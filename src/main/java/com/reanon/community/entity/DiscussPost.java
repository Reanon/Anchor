package com.reanon.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 讨论贴, 对应数据库表 `discuss_post`
 *
 * @author reanon
 * @create 2021-07-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DiscussPost {
    private int id;

    private int userId;

    private String title;

    private String content;

    private int type;

    private int status;

    private Date createTime;

    private int commentCount;

    private double score;
}
