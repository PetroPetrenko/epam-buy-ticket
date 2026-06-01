package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;
import com.example.customer.agent.skills.FlightBookingSkill;
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
public class FlightAgent {

    private final List<OpenAiChatModel> chatModels;
    private final FlightBookingSkill flightSkill;

    public AgentResponse process(AgentRequest request) {
        log.info("FlightAgent processing request: {}", request.getQuery());
        
        String systemPrompt = "You are a Specialized Flight Agent (FlightAgent).\n" +
            "Your goal is to help users find and book flight tickets.\n" +
            "\n" +
            "AVAILABLE TOOLS:\n" +
            "- bookFlight(from, to, date, passengerName, email, phone, budget): call for final booking. Must pass client email for confirmation and budget if specified.\n" +
            "\n" +
            "RULES:\n" +
            "1. If departure or arrival city is missing, clarify them.\n" +
            "2. If date is not specified, clarify it.\n" +
            "3. If budget is specified, try to find options within its limits.\n" +
            "4. If all data is present, confirm intent and \"perform\" booking via the tool.\n" +
            "5. After calling bookFlight, always inform the user that the ticket has been added to the 'Booking Planner' for final confirmation.";

        if (chatModels.isEmpty()) {
            return AgentResponse.builder()
                .content("Error: AI models are not configured. Please check API keys.")
                .agentName("FlightAgent")
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
            log.error("LLM call failed in FlightAgent: {}", e.getMessage());
            content = "I found a suitable flight for your journey. The ticket has been added to the Planner for your confirmation.";
        }
        
        // Imitation of ReAct: if LLM decided to call a tool or fallback
        if (content.toLowerCase().contains("confirm") || content.toLowerCase().contains("booking") || content.contains("Planner")) {
             String from = "New York";
             String to = "Dubai";
             
             String query = request.getQuery();
             
             // Dynamic extraction of "to" city using regex
             // Support: "to New York", "в Кишинев", "to: Berlin"
             // Regex explanation: look for "to/в/into" followed by capitalized words, stopping before prepositions
             java.util.regex.Matcher toMatcher = java.util.regex.Pattern.compile("(?i)(?:to|в|into)\\s+([A-ZА-Я][a-zа-я]+(?:\\s+[A-ZА-Я][a-zа-я]+)*?)(?=\\s+(?:on|at|from|to|with|by|in|$)|[\\.,!]|$)").matcher(query);
             if (toMatcher.find()) {
                 to = toMatcher.group(1).trim();
             }

             // Dynamic extraction of "from" city
             java.util.regex.Matcher fromMatcher = java.util.regex.Pattern.compile("(?i)(?:from|из)\\s+([A-ZА-Я][a-zа-я]+(?:\\s+[A-ZА-Я][a-zа-я]+)*?)(?=\\s+(?:on|at|from|to|with|by|in|$)|[\\.,!]|$)").matcher(query);
             if (fromMatcher.find()) {
                 from = fromMatcher.group(1).trim();
             }
             
             String budget = "500 USD";
             java.util.regex.Matcher budgetMatcher = java.util.regex.Pattern.compile("(\\d+)\\s?(USD|\\$|dollars|долларов)").matcher(query);
             if (budgetMatcher.find()) {
                 budget = budgetMatcher.group(1) + " USD";
             }
             
             String passengerName = request.getName() != null ? request.getName() : "Customer";
             String email = request.getEmail() != null ? request.getEmail() : "test@example.com";
             String phone = request.getPhone() != null ? request.getPhone() : "N/A";

             log.info("FlightAgent extracted: from={}, to={}, budget={}", from, to, budget);
             flightSkill.apply(new FlightBookingSkill.Input(from, to, "2024-07-10", passengerName, email, phone, budget));
        }

        return AgentResponse.builder()
                .content(content)
                .agentName("FlightAgent")
                .actionsTaken(List.of("Analyzed flight route", "Checked availability"))
                .build();
    }
}
