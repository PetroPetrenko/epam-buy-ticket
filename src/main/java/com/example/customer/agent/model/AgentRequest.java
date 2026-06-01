package com.example.customer.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    private String query;
    private Long customerId;
    private String name;
    private String email;
    private String phone;
}
