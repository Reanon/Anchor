package com.reanon.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 登录凭证
 *
 * @author reanon
 * @create 2021-07-03
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginTicket {
    private int id;
    private int userId;
    // 凭证: 随机字符串
    private String ticket;
    // 状态（是否有效）
    private int status;
    // 过期时间
    private Date expired;
}
