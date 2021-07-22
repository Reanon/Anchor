package com.reanon.community.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

/**
 * 配置类的方式来注入Bean
 * https://www.cnblogs.com/hi3254014978/p/14055771.html
 * @author reanon
 * @create 2021-07-11
 */
@Configuration
public class ElasticsearchConfig {
    @Value("${elasticsearch.url}")
    private String esUrl;

    @Bean
    RestHighLevelClient client() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(esUrl) // elasticsearch地址
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
