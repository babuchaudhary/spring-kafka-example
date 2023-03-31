package com.example.springkafkaexample.controller;

import com.example.springkafkaexample.service.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/javainuse-kafka/")
public class ApacheKafkaWebController {
    @Autowired
    KafkaSender kafkaSender;

    @GetMapping(value = "/producer")
    public ResponseEntity<?> producer(@RequestParam("message") String message) throws ExecutionException, InterruptedException {
        kafkaSender.send(message);

        return ResponseEntity.ok(String.format("Message - { %s } sent to the Kafka Topic Successfully", message));
    }

}
