package com.example.customer.service;

import com.example.customer.repository.CustomerRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final CustomerRepository repo;

    public String exportToCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            String[] header = {"ID", "Name", "Email", "Address", "Notes", "Created At"};
            writer.writeNext(header);

            repo.findAll().forEach(c -> {
                String[] data = {
                        String.valueOf(c.getId()),
                        c.getName(),
                        c.getEmail(),
                        c.getAddress(),
                        c.getNotes(),
                        c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
                };
                writer.writeNext(data);
            });
        } catch (Exception e) {
            throw new RuntimeException("Fail to export CSV", e);
        }
        return sw.toString();
    }
}
