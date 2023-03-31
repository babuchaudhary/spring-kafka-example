package com.example.springkafkaexample.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class KafkaClient {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    public static final String EVENT_ID_HEADER_KEY = "demo_eventIdHeader";

    public SendResult sendMessageWithTransaction(String key, String data, String outboundTopic) throws ExecutionException, InterruptedException {
        return sendMessage("Transactional", kafkaTemplate, key, data, outboundTopic);
    }

    public SendResult sendMessage(String type, KafkaTemplate<String, Object> kafkaTemplate, String key, String data, String outboundTopic) throws ExecutionException, InterruptedException {
        try {
            String payload = "message: " + data  + ", eventId: " + UUID.randomUUID();
            final ProducerRecord<String, Object> record =
                    new ProducerRecord<>(outboundTopic, key, payload);
            final SendResult result = (SendResult) kafkaTemplate.send(record).get();
            final RecordMetadata metadata = result.getRecordMetadata();
            log.info(String.format("Sent %s record(key=%s value=%s) meta(topic=%s, partition=%d, offset=%d)",
                    type, record.key(), record.value(), metadata.topic(), metadata.partition(), metadata.offset()));
            return result;
        } catch (Exception e) {
            log.error("Error sending message to topic: " + outboundTopic, e);
            throw e;
        }
    }
}
