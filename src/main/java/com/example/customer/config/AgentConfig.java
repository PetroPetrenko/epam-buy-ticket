package com.example.customer.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AgentConfig {

    @Bean
    public ChatMemory chatMemory() {
        // В будущем можно заменить на RedisChatMemory для персистентности
        return new InMemoryChatMemory();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
