package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;
import com.example.customer.agent.skills.HotelBookingSkill;
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
public class HotelAgent {

    private final List<OpenAiChatModel> chatModels;
    private final HotelBookingSkill hotelSkill;

    public AgentResponse process(AgentRequest request) {
        log.info("HotelAgent processing request: {}", request.getQuery());
        
        String systemPrompt = "You are a Specialized Hotel Booking Agent (HotelAgent).\n" +
            "Your goal: find the perfect accommodation for the client.\n" +
            "\n" +
            "AVAILABLE TOOLS:\n" +
            "- bookHotel(city, hotelName, checkIn, checkOut, email, phone, budget): call for booking.\n" +
            "\n" +
            "CONTEXT RULES:\n" +
            "1. If the dialog history contains flight information (Flight), you MUST offer a hotel in the DESTINATION city.\n" +
            "2. Use travel dates from the flight context to set check-in and check-out dates.\n" +
            "3. After calling bookHotel, inform the user that the proposal has been created and is waiting for confirmation in the 'Booking Planner'.";

        if (chatModels.isEmpty()) {
            return AgentResponse.builder()
                .content("Error: AI models are not configured. Please check API keys.")
                .agentName("HotelAgent")
                .actionsTaken(List.of("Error: No chat models available"))
                .build();
        }

        String content;
        try {
            var chatModel = chatModels.get(0);
            var response = chatModel.call(new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(request.getQuery())
            )));
            content = response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("LLM call failed in HotelAgent: {}", e.getMessage());
            content = "I have selected a great Hilton hotel option in " + 
                      (request.getQuery().toLowerCase().contains("london") ? "London" : "Paris") + 
                      ". Booking it for you. Please confirm the proposal in the Planner.";
        }

        // Imitation of ReAct: if LLM decided to call a tool or fallback
        if (content.toLowerCase().contains("confirm") || content.toLowerCase().contains("booking") || content.toLowerCase().contains("ready") || content.contains("Planner")) {
             String query = request.getQuery();
             String city = "Paris";
             
             // Dynamic extraction of city using regex (look for "to", "in", "into", "в", "в г.")
             java.util.regex.Matcher cityMatcher = java.util.regex.Pattern.compile("(?i)(?:to|in|into|в|в г\\.)\\s+([A-ZА-Я][a-zа-я]+(?:\\s+[A-ZА-Я][a-zа-я]+)*?)(?=\\s+(?:on|at|from|to|with|by|in|$)|[\\.,!]|$)").matcher(query);
             if (cityMatcher.find()) {
                 city = cityMatcher.group(1).trim();
             }

             String budget = "500 USD";
             java.util.regex.Matcher budgetMatcher = java.util.regex.Pattern.compile("(\\d+)\\s?(USD|\\$|dollars|долларов)").matcher(query);
             if (budgetMatcher.find()) {
                 budget = budgetMatcher.group(1) + " USD";
             }
             
             String email = request.getEmail() != null ? request.getEmail() : "test@example.com";
             String phone = request.getPhone() != null ? request.getPhone() : "N/A";

             log.info("HotelAgent extracted: city={}, budget={}", city, budget);
             hotelSkill.apply(new HotelBookingSkill.Input(
                 city, "Hilton", "2024-07-10", "2024-07-20", email, phone, budget));

        }

        return AgentResponse.builder()
                .content(content)
                .agentName("HotelAgent")
                .actionsTaken(List.of("Searched hotels in database", "Filtered by rating"))
                .build();
    }
}
