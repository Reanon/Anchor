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
    private String ticket; // 凭证
    private int status; // 状态（是否有效）
    private Date expired; // 过期时间
}
