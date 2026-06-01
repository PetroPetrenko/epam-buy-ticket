package com.example.customer.controller;

import com.example.customer.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Tag(name = "Export API", description = "Экспорт данных")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/customers/csv")
    @Operation(summary = "Экспорт списка клиентов в CSV", 
               description = "Генерирует CSV файл со всеми данными клиентов")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = exportService.exportToCsv();
        byte[] data = csv.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
