package com.reanon.community.event;

import com.alibaba.fastjson.JSONObject;
import com.reanon.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件的生产者
 */
@Component
public class EventProducer {
    // 注入 Kafka 组件
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * kafka 将事件存入topic
     *
     * @param event 事件
     */
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
