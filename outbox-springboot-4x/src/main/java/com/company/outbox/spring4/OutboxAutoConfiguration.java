package com.company.outbox.spring4;

import com.company.outbox.core.MessagePublisher;
import com.company.outbox.core.OutboxProcessor;
import com.company.outbox.core.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.company.outbox.spring4")
public class OutboxAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public OutboxProcessor outboxProcessor(OutboxRepository outboxRepository, 
                                         MessagePublisher messagePublisher) {
        return new OutboxProcessor(outboxRepository, messagePublisher);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}