package com.example.customer.agent;

import com.example.customer.agent.model.AgentResponse;
import com.example.customer.service.CustomerService;
import com.example.customer.dto.CustomerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MultiAgentSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testFlightAndHotelOrchestrationWithRag() throws Exception {
        // Mock customer data for RAG context
        CustomerDto.Response mockCustomer = new CustomerDto.Response(
                1L, "Test User", "test@example.com", "London", "Prefers business class and 5-star hotels", null
        );
        when(customerService.findById(anyLong())).thenReturn(mockCustomer);

        Map<String, Object> request = Map.of(
                "query", "Забронируй мне билет и отель в Париже",
                "customerId", 1
        );

        mockMvc.perform(post("/api/v1/agents/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentName").value("MainOrchestrator"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.actionsTaken").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAgentTraining() throws Exception {
        Map<String, String> trainingData = Map.of(
                "example", "If user says 'fly', use FlightAgent"
        );

        mockMvc.perform(post("/api/v1/agents/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainingData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("успешно дообучен")));
    }
}
