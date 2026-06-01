package com.example.customer.config;

import com.example.customer.entity.Customer;
import com.example.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Сидер для заполнения базы данных начальными данными.
 * Запускается только если база пуста.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            log.info("Database seeding is disabled for local testing to avoid H2 table issues.");
        };
    }
}
