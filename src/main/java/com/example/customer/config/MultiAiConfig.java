package com.example.customer.config;

import lombok.Data;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class MultiAiConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.ai")
    public AiProvidersProperties aiProvidersProperties() {
        return new AiProvidersProperties();
    }

    @Data
    public static class AiProvidersProperties {
        private List<ProviderConfig> providers = new ArrayList<>();

        @Data
        public static class ProviderConfig {
            private String name;
            private String apiKey;
            private String baseUrl;
            private String model;
        }
    }

    @Bean
    @Primary
    public OpenAiChatModel primaryChatModel(List<OpenAiChatModel> chatModels) {
        return chatModels.get(0);
    }

    @Bean
    public List<OpenAiChatModel> chatModels(AiProvidersProperties properties) {
        if (properties.getProviders().isEmpty()) {
            // Fallback: Создаем пустой список или мок, если провайдеры не настроены
            return new ArrayList<>();
        }
        return properties.getProviders().stream()
                .map(config -> {
                    String baseUrl = config.getBaseUrl();
                    if (baseUrl == null || baseUrl.isBlank()) {
                        baseUrl = "https://api.openai.com/v1";
                    }
                    var api = new OpenAiApi(baseUrl, config.getApiKey());
                    var options = org.springframework.ai.openai.OpenAiChatOptions.builder()
                            .withModel(config.getModel())
                            .build();
                    return new OpenAiChatModel(api, options);
                })
                .collect(Collectors.toList());
    }
}
