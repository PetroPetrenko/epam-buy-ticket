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
public class ScriptAgent {
    private final List<OpenAiChatModel> chatModels;

    public AgentResponse process(AgentRequest request) {
        log.info("ScriptAgent creating script based on Hollywood standards...");
        
        String systemPrompt = "Ты - Профессиональный Голливудский Сценарист (ScriptAgent).\n" +
            "Твоя специализация: создание захватывающих сценариев с использованием лучших мировых практик.\n" +
            "\n" +
            "МЕТОДОЛОГИИ:\n" +
            "1. 3-Act Structure (Setup, Confrontation, Resolution).\n" +
            "2. The Hero's Journey (Call to Adventure, Crossing the Threshold, Ordeal, Return).\n" +
            "3. Save the Cat! (Beat Sheet: Opening Image, Theme Stated, B-Story, All is Lost).\n" +
            "\n" +
            "ЗАДАЧА:\n" +
            "- Напиши подробный сценарий для видео на основе запроса пользователя.\n" +
            "- Включи описание визуальных образов (для ImageAgent) и динамики (для MotionAgent).\n" +
            "- Соблюдай драматический ритм и эмоциональную дугу.";

        var response = chatModels.get(0).call(new Prompt(List.of(
            new SystemMessage(systemPrompt),
            new UserMessage(request.getQuery())
        )));

        return AgentResponse.builder()
                .content(response.getResult().getOutput().getContent())
                .agentName("ScriptAgent")
                .actionsTaken(List.of("Applied Hero's Journey framework", "Structured into 3 Acts"))
                .build();
    }
}
