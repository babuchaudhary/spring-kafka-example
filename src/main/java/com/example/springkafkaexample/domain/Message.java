package com.example.springkafkaexample.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_text")
    private String messageText;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
