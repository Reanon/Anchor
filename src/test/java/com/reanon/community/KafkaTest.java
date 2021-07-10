package com.reanon.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Autowired
    private KafkaProducer kafkaProducer;


    @Test
    @DisplayName("测试kafka")
    public void testKafka() {
        // 生产者生产消息：指定 topic 为 test
        kafkaProducer.sendMessage("test", "你好！！！");
        kafkaProducer.sendMessage("test", "hi");

        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

// 封装生产者
@Component
class KafkaProducer {
    // Spring 整合了, 可以直接注入
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

// 消费者
@Component
class KafkaConsumer {
    // 指定主题
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        // 读取消息
        System.out.println(record.value());
    }
}
