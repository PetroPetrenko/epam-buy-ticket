package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final MainAgentOrchestrator orchestrator;

    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public AgentResponse chat(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.get("query");
        Integer customerIdInt = (Integer) payload.get("customerId");
        Long customerId = customerIdInt != null ? customerIdInt.longValue() : null;

        return orchestrator.handleRequest(AgentRequest.builder()
                .query(query)
                .customerId(customerId)
                .build());
    }

    @PostMapping("/train")
    public String train(@RequestBody Map<String, String> trainingData) {
        // Эмуляция дообучения (Few-shot learning / Fine-tuning)
        // В реальности данные могли бы сохраняться в VectorStore для RAG или использоваться для Fine-tuning API
        String example = trainingData.get("example");
        return "Агент успешно дообучен на примере: " + (example.length() > 50 ? example.substring(0, 50) + "..." : example);
    }
}
