package com.example.springkafkaexample.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${app.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.group-id}")
    private String groupId;

    @Value("${app.kafka.producer.idem-potence}")
    private String idemPotence;

    @Value("${app.kafka.use-truststore:true}")
    private boolean useTruststore;

    @Value("${app.kafka.truststore.location}")
    private String truststoreLocation;

    @Value("${app.kafka.truststore.password}")
    private String truststorePassword;

    @Value("${app.kafka.keystore.location}")
    private String keystoreLocation;

    @Value("${app.kafka.keystore.password}")
    private String keystorePassword;

    @Value("${app.kafka.key.password}")
    private String keyPassword;

    @Value("${app.kafka.producer.transaction-id-prefix}")
    private String transactionPrefix;
    private int MULTIPLIER = 2; // 5 mins, 15 mins, 45 mins, DLT
    private int RETRY_INITIAL = 1000; // 300000 = 5 mins
    private int RETRY_MAX = 86400000; // 86400000 = 1 day
    @Bean
 @Primary
 public ProducerFactory<String, Object> producerFactory() {
 return new DefaultKafkaProducerFactory<>(producerConfigs());
 }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "aaa1111");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        //config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "dev-1");
        if (useTruststore) {
            config.putAll(getSslConfigProps());
        }
        return config;
    }


 @Bean
 @Primary
 public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
 }

 @Primary
 @Bean
 public KafkaTransactionManager<String, Object> kafkaTransactionManager() {
    return new KafkaTransactionManager<>(producerFactory());
 }

 @Bean
    @Primary
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configMap = consumerConfigs();
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "au.gov.austrac.model");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        if (useTruststore)
            config.putAll(getSslConfigProps());
        return config;
    }

    private Map<String, Object> getSslConfigProps() {
        Map<String, Object> sslConfigProps = new HashMap<>();
        try {
            final String classPathStr = "classpath";
            if (StringUtils.startsWithIgnoreCase(keystoreLocation, classPathStr)) {
                Resource keyStore = new ClassPathResource(extractFileName(keystoreLocation));
                sslConfigProps.put("ssl.keystore.location", keyStore.getFile().getAbsolutePath());
            } else {
                sslConfigProps.put("ssl.keystore.location", keystoreLocation);
            }
            if (StringUtils.startsWithIgnoreCase(truststoreLocation, classPathStr)) {
                Resource truststore = new ClassPathResource(extractFileName(truststoreLocation));
                sslConfigProps.put("ssl.truststore.location", truststore.getFile().getAbsolutePath());
            } else {
                sslConfigProps.put("ssl.truststore.location", truststoreLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sslConfigProps.put("security.protocol", "SSL");
        sslConfigProps.put("ssl.truststore.password", truststorePassword);
        sslConfigProps.put("ssl.keystore.password", keystorePassword);
        sslConfigProps.put("ssl.key.password", keyPassword);
        sslConfigProps.put("ssl.endpoint.identification.algorithm", "");
        return sslConfigProps;
    }

    private String extractFileName(String storeLocation) {
        var arrOfString = StringUtils.split(storeLocation, "/");
        if (Objects.isNull(arrOfString)) {
            return null;
        }
        return arrOfString[arrOfString.length - 1];
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() throws IOException {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
