package com.example.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Аннотация для валидации заметок клиента.
 * Заметки не должны содержать запрещенных слов (спам-фильтр)
 * и должны соответствовать корпоративному стилю.
 */
@Documented
@Constraint(validatedBy = CustomerNotesValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNotes {
    String message() default "Notes contain forbidden words or invalid format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * Список запрещенных слов (для примера)
     */
    String[] forbiddenWords() default {"spam", "scam", "advertisement"};
}
