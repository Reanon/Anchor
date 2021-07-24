package com.reanon.community.actuator;

import com.reanon.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库性能监控
 * 访问: /anchor/actuator/datasource, 查看数据库是否连接成功
 * @author reanon
 * @create 2021-07-24
 */
@Component
@Endpoint(id = "datasource")
public class DatabaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    /**
     * 利用获取数据库连接情况，示例来演示 actuator 自定义端点用法
     */
    @ReadOperation
    public String CheckConnection() {
        try(
                Connection connection = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "获取连接成功！");
        } catch (SQLException e) {
            logger.error("获取连接失败:" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }
    }
}
