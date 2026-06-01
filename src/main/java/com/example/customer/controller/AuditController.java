package com.example.customer.controller;

import com.example.customer.dto.CustomerDto;
import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit API", description = "Просмотр истории изменений")
public class AuditController {

    private final CustomerService customerService;

    @GetMapping("/customers/{id}")
    @Operation(summary = "Получить историю изменений клиента", 
               description = "Возвращает все версии данных клиента с указанием типа изменения (создание, обновление, удаление)")
    public ResponseEntity<List<CustomerDto.AuditResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getAuditHistory(id));
    }
}
