package com.example.springkafkaexample.repository;

import com.example.springkafkaexample.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
