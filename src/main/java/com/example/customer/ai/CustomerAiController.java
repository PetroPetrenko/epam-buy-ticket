package com.example.customer.ai;

import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer AI API", description = "AI-функции для работы с клиентами")
public class CustomerAiController {

    private final CustomerAiService aiService;
    private final CustomerService customerService;

    @GetMapping("/{id}/ai/summarize")
    @Operation(summary = "Суммаризировать заметки о клиенте с помощью AI")
    public ResponseEntity<String> summarize(@PathVariable Long id) {
        var customer = customerService.findById(id);
        return ResponseEntity.ok(aiService.summarizeNotes(customer.notes()));
    }

    @GetMapping("/{id}/ai/suggest-response")
    @Operation(summary = "Предложить ответ клиенту на основе его заметок")
    public ResponseEntity<String> suggest(@PathVariable Long id) {
        var customer = customerService.findById(id);
        return ResponseEntity.ok(aiService.suggestResponse(customer.notes()));
    }

    @GetMapping("/ai/usage")
    @Operation(summary = "Получить статистику использования токенов по моделям")
    public ResponseEntity<Map<String, CustomerAiService.TokenUsage>> getUsageStats() {
        return ResponseEntity.ok(aiService.getUsageStats());
    }
}
