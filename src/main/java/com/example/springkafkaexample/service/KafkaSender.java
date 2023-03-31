package com.example.springkafkaexample.service;

import com.example.springkafkaexample.config.KafkaClient;
import com.example.springkafkaexample.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaSender {

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaTransactionManager<String, Object> kafkaTransactionManager;

    @Value("${app.kafka.topics.topic-name}")
    private String kafkaTopic;

    @Autowired
    @Qualifier("dbTransactionManager")
    private PlatformTransactionManager dbTransactionManager;

    @Autowired
    private MessageDbService messageDbService;

    //@Transactional("kafkaTransactionManager")
    public void send(String messageText) throws ExecutionException, InterruptedException {

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus dbTransactionStatus = dbTransactionManager.getTransaction(transactionDefinition);
        TransactionStatus kafkaTransactionStatus = kafkaTransactionManager.getTransaction(transactionDefinition);
        try {
            Message message1 = new Message();
            message1.setMessageText(messageText);
            message1.setCreatedDate(LocalDateTime.now());
            messageDbService.saveMessage(message1);
            kafkaTemplate.send(kafkaTopic, "key1",  messageText);
            if(messageText.contains("test")) {
                throw new RuntimeException("message contains test");
            }

            kafkaTransactionManager.commit(kafkaTransactionStatus);
            dbTransactionManager.commit(dbTransactionStatus);

        }
        catch (Exception e) {
            kafkaTransactionManager.rollback(kafkaTransactionStatus);
            dbTransactionManager.rollback(dbTransactionStatus);
            throw e;
        }
    //    throw new RuntimeException("Error");
    }
}
