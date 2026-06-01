package com.example.customer.controller;

import com.example.customer.dto.CustomerDto;
import com.example.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/customers")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer API", description = "Управление данными клиентов")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Получить список клиентов с пагинацией (Offset-based)")
    public ResponseEntity<CustomerDto.PageResponse<CustomerDto.Response>> findAll(
            @RequestParam(defaultValue = "0")  @Min(0)        int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name")               String sort
    ) {
        log.debug("GET /api/v1/customers page={} size={} sort={}", page, size, sort);
        return ResponseEntity.ok(customerService.findAll(page, size, sort));
    }

    @GetMapping("/seek")
    @Operation(summary = "Получить список клиентов через Keyset Pagination (Seek Method)",
               description = "Более эффективная пагинация для больших объемов данных. Использует последний ID для получения следующей страницы.")
    public ResponseEntity<CustomerDto.SeekResponse<CustomerDto.Response>> findAllSeek(
            @RequestParam(required = false)                    Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        log.debug("GET /api/v1/customers/seek lastId={} size={}", lastId, size);
        return ResponseEntity.ok(customerService.findAllSeek(lastId, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить клиента по ID")
    public ResponseEntity<CustomerDto.Response> findById(@PathVariable Long id) {
        log.debug("GET /api/v1/customers/{}", id);
        return ResponseEntity.ok(customerService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Быстрый поиск по имени, email или адресу")
    public ResponseEntity<CustomerDto.PageResponse<CustomerDto.Response>> search(
            @RequestParam("q")                                  String keyword,
            @RequestParam(defaultValue = "0")  @Min(0)         int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        log.debug("GET /api/v1/customers/search q='{}' page={} size={}", keyword, page, size);
        return ResponseEntity.ok(customerService.search(keyword, page, size));
    }

    @PostMapping
    @Operation(summary = "Создать нового клиента")
    public ResponseEntity<CustomerDto.Response> create(
            @RequestBody @Valid CustomerDto.CreateRequest req,
            UriComponentsBuilder ucb
    ) {
        log.info("POST /api/v1/customers - creating customer with email: {}", req.email());
        CustomerDto.Response saved = customerService.create(req);
        var location = ucb.path("/api/v1/customers/{id}").buildAndExpand(saved.id()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные клиента")
    public ResponseEntity<CustomerDto.Response> update(
            @PathVariable Long id,
            @RequestBody @Valid CustomerDto.UpdateRequest req
    ) {
        log.info("PUT /api/v1/customers/{} - updating customer", id);
        return ResponseEntity.ok(customerService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить клиента")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("DELETE /api/v1/customers/{} - deleting customer", id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
