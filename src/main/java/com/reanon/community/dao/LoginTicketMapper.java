package com.reanon.community.dao;

import com.reanon.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

// 后期采用 Redis 来保存登录凭证，不再从 MySQL 中直接查询
@Mapper
@Deprecated // 废弃注解
public interface LoginTicketMapper {
    // 注解的方式写sql
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"})
    // 注解方式通过以下方式自增id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({"select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({"update login_ticket set status = #{status} where ticket=#{ticket}"})
    int updateStatus(String ticket, int status);
}
