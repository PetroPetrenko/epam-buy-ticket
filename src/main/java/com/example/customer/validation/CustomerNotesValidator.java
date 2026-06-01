package com.example.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class CustomerNotesValidator implements ConstraintValidator<ValidNotes, String> {

    private String[] forbiddenWords;

    @Override
    public void initialize(ValidNotes constraintAnnotation) {
        this.forbiddenWords = constraintAnnotation.forbiddenWords();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank проверит это отдельно, если нужно
        }

        String lowerValue = value.toLowerCase();
        
        // Проверка на запрещенные слова
        boolean hasForbiddenWord = Arrays.stream(forbiddenWords)
                .anyMatch(lowerValue::contains);

        if (hasForbiddenWord) {
            // Можно динамически менять сообщение об ошибке
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Notes contain sensitive or forbidden content")
                   .addConstraintViolation();
            return false;
        }

        // Пример дополнительной бизнес-логики: заметка не должна состоять только из цифр
        if (value.matches("^[0-9]+$")) {
            return false;
        }

        return true;
    }
}
