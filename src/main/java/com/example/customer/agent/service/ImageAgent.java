package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageAgent {
    private final List<OpenAiChatModel> chatModels;

    public AgentResponse process(AgentRequest request) {
        log.info("ImageAgent generating visual prompts...");
        
        String systemPrompt = "Ты - Агент по визуальному контенту (ImageAgent).\n" +
            "Твоя цель: превращать сценарии в детализированные промпты для генерации изображений (Midjourney, Stable Diffusion).\n" +
            "\n" +
            "ИНСТРУКЦИИ:\n" +
            "- Используй технические параметры: освещение (cinematic lighting), камера (8k, wide angle), стиль (hyper-realistic).\n" +
            "- Описывай композицию и цветовую палитру.";

        var response = chatModels.get(0).call(new Prompt(List.of(
            new SystemMessage(systemPrompt),
            new UserMessage(request.getQuery())
        )));

        return AgentResponse.builder()
                .content(response.getResult().getOutput().getContent())
                .agentName("ImageAgent")
                .actionsTaken(List.of("Generated SD/MJ prompts", "Optimized visual composition"))
                .build();
    }
}
