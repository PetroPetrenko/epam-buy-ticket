package com.example.customer.agent.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String content;
    private List<String> actionsTaken;
    private String agentName;
}
