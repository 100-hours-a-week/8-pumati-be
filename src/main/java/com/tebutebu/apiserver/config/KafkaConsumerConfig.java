package com.tebutebu.apiserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.mail-send-dlq}")
    private String mailSendDlqTopic;

    private static final int MAX_RETRY_COUNT = 2;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    Headers headers = record.headers();

                    // x-error-code
                    headers.remove("x-error-code");
                    headers.add(new RecordHeader("x-error-code", ex.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8)));

                    // x-error-message
                    headers.remove("x-error-message");
                    headers.add(new RecordHeader("x-error-message",
                            Optional.ofNullable(ex.getMessage()).orElse("unknown").getBytes(StandardCharsets.UTF_8)));

                    // x-retry-count
                    String retryCountStr = Optional.ofNullable(headers.lastHeader("x-retry-count"))
                            .map(h -> new String(h.value(), StandardCharsets.UTF_8))
                            .orElse("0");

                    int currentRetryCount = Integer.parseInt(retryCountStr);
                    int totalRetryCount = MAX_RETRY_COUNT + currentRetryCount + 1; // +1 for initial try
                    headers.remove("x-retry-count");
                    headers.add(new RecordHeader("x-retry-count", String.valueOf(totalRetryCount).getBytes(StandardCharsets.UTF_8)));

                    // x-retry-stage
                    headers.remove("x-retry-stage");
                    headers.add(new RecordHeader("x-retry-stage", "dlq".getBytes(StandardCharsets.UTF_8)));

                    log.warn("Sending message to DLQ [retryCount={}, exception={}]", totalRetryCount, ex.getMessage());

                    return new TopicPartition(mailSendDlqTopic, 0);
                }
        );

        return new DefaultErrorHandler(deadLetterPublishingRecoverer, new FixedBackOff(1000L, MAX_RETRY_COUNT));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler,
            @Value("${spring.kafka.listener.concurrency}") int concurrency
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.setConcurrency(concurrency);
        return factory;
    }

}
