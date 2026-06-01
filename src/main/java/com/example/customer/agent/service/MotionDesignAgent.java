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
public class MotionDesignAgent {
    private final List<OpenAiChatModel> chatModels;

    public AgentResponse process(AgentRequest request) {
        log.info("MotionDesignAgent animating content...");
        
        String systemPrompt = "Ты - Агент по моушн-дизайну (MotionDesignAgent).\n" +
            "Твоя цель: добавить динамику, титры и эффекты.\n" +
            "\n" +
            "ИНСТРУКЦИИ:\n" +
            "- Описывай анимацию текста и графики.\n" +
            "- Используй принципы анимации (Easing, Squash and Stretch).\n" +
            "- Подготовь JSON/Lottie структуру или инструкции для After Effects.";

        var response = chatModels.get(0).call(new Prompt(List.of(
            new SystemMessage(systemPrompt),
            new UserMessage(request.getQuery())
        )));

        return AgentResponse.builder()
                .content(response.getResult().getOutput().getContent())
                .agentName("MotionDesignAgent")
                .actionsTaken(List.of("Defined animation curves", "Created Lottie-ready structure"))
                .build();
    }
}
