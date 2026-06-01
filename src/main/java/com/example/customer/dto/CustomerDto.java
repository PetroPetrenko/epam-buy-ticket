package com.example.customer.dto;

import com.example.customer.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "DTO для работы с данными клиентов")
public sealed interface CustomerDto {

    @Schema(description = "Customer creation request")
    record CreateRequest(
            @Schema(example = "John Doe") @NotBlank @Size(max = 100) String name,
            @Schema(example = "john@example.com") @NotBlank @Email @Size(max = 150) String email,
            @Schema(example = "+12125550199") @Size(max = 20) String phone,
            @Schema(example = "123 Broadway, New York, NY") @Size(max = 255) String address,
            @com.example.customer.validation.ValidNotes String notes
    ) implements CustomerDto {}

    @Schema(description = "Customer update request")
    record UpdateRequest(
            @Schema(example = "John Doe") @NotBlank @Size(max = 100) String name,
            @Schema(example = "john@example.com") @NotBlank @Email @Size(max = 150) String email,
            @Schema(example = "+12125550199") @Size(max = 20) String phone,
            @Schema(example = "456 5th Ave, New York, NY") @Size(max = 255) String address,
            @com.example.customer.validation.ValidNotes String notes
    ) implements CustomerDto {}

    @Schema(description = "Данные клиента")
    record Response(
            Long id,
            String name,
            String email,
            String phone,
            String address,
            String notes,
            String createdAt
    ) implements CustomerDto {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getNotes(),
                    customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null
            );
        }
    }

    @Schema(description = "Ответ со списком (Offset-based пагинация)")
    record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) implements CustomerDto {}

    @Schema(description = "Ответ со списком (Keyset/Seek пагинация)")
    record SeekResponse<T>(
            List<T> content,
            @Schema(description = "ID последнего элемента в списке (используется как lastId для следующей страницы)")
            Long lastId,
            @Schema(description = "Есть ли следующая страница")
            boolean hasNext
    ) implements CustomerDto {}

    @Schema(description = "Ответ с историей изменений")
    record AuditResponse(
            Integer revisionNumber,
            String revisionType,
            Response customer,
            String timestamp
    ) implements CustomerDto {}
}
