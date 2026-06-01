package com.example.customer.ai;

import com.example.customer.config.MultiAiConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAiService {

    private final List<OpenAiChatModel> chatModels;
    private final MultiAiConfig.AiProvidersProperties aiProperties;

    @Getter
    private final Map<String, TokenUsage> usageStats = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenUsage {
        private AtomicLong promptTokens = new AtomicLong(0);
        private AtomicLong completionTokens = new AtomicLong(0);
        private AtomicLong totalTokens = new AtomicLong(0);
    }

    public String summarizeNotes(String notes) {
        if (notes == null || notes.isEmpty()) {
            return "No notes to summarize.";
        }
        return executeWithFailover(notes, "Summarize the following customer notes into a concise 1-2 sentence summary.");
    }

    public String suggestResponse(String notes) {
        if (notes == null || notes.isEmpty()) {
            return "Cannot suggest a response without notes.";
        }
        return executeWithFailover(notes, "Based on these customer notes, suggest a professional and friendly email response to the customer.");
    }

    private String executeWithFailover(String notes, String systemPrompt) {
        StringBuilder errorLog = new StringBuilder();

        for (int i = 0; i < chatModels.size(); i++) {
            OpenAiChatModel model = chatModels.get(i);
            var providerConfig = aiProperties.getProviders().get(i);
            
            log.debug("Trying AI provider: {} (model: {})", providerConfig.getName(), providerConfig.getModel());

            try {
                if (providerConfig.getApiKey() == null || providerConfig.getApiKey().equals("demo")) {
                    throw new RuntimeException("Demo key used");
                }

                var options = OpenAiChatOptions.builder()
                        .withModel(providerConfig.getModel())
                        .withTemperature(0.7f)
                        .build();

                Prompt prompt = new Prompt(new UserMessage(systemPrompt + "\n\nNotes: " + notes), options);
                ChatResponse response = model.call(prompt);
                
                trackUsage(response, providerConfig.getName());
                return response.getResult().getOutput().getContent();

            } catch (Exception e) {
                log.warn("AI provider {} failed: {}", providerConfig.getName(), e.getMessage());
                errorLog.append(providerConfig.getName()).append(": ").append(e.getMessage()).append("; ");
            }
        }

        log.error("All AI providers failed. Error log: {}", errorLog);
        return "[FALLBACK MODE] All AI networks are unavailable or out of tokens. Notes preview: " + 
               (notes.length() > 50 ? notes.substring(0, 50) + "..." : notes);
    }

    private void trackUsage(ChatResponse response, String providerName) {
        Usage usage = response.getMetadata().getUsage();
        if (usage != null) {
            TokenUsage stats = usageStats.computeIfAbsent(providerName, k -> new TokenUsage());
            
            stats.getPromptTokens().addAndGet(usage.getPromptTokens() != null ? usage.getPromptTokens() : 0);
            stats.getCompletionTokens().addAndGet(usage.getGenerationTokens() != null ? usage.getGenerationTokens() : 0);
            stats.getTotalTokens().addAndGet(usage.getTotalTokens() != null ? usage.getTotalTokens() : 0);
        }
    }
}
