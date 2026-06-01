package com.example.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public sealed class CustomerException extends RuntimeException {

    public CustomerException(String message) {
        super(message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static final class NotFound extends CustomerException {
        public NotFound(Long id) {
            super("Customer with ID " + id + " not found");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static final class EmailAlreadyExists extends CustomerException {
        public EmailAlreadyExists(String email) {
            super("Customer with email " + email + " already exists");
        }
    }
}
