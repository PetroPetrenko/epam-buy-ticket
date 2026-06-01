package com.example.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Управление кэшем приложения")
public class CacheController {

    private final CacheManager cacheManager;

    @DeleteMapping("/clear")
    @Operation(summary = "Полная очистка кэша клиентов", 
               description = "Удаляет все записи из кэша 'customers'. Полезно при ручном изменении базы данных.")
    public ResponseEntity<String> clearCache() {
        log.info("Manual cache clear requested");
        Objects.requireNonNull(cacheManager.getCache("customers")).clear();
        log.info("Cache 'customers' has been cleared");
        return ResponseEntity.ok("Cache 'customers' cleared successfully");
    }
}
