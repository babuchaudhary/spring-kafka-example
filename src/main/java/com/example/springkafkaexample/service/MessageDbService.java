package com.example.springkafkaexample.service;

import com.example.springkafkaexample.domain.Message;
import com.example.springkafkaexample.repository.MessageRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageDbService {

    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
}
