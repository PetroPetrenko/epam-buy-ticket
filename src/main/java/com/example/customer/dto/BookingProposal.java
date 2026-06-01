package com.example.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingProposal {
    private String id;
    private String type; // "HOTEL" or "FLIGHT"
    private String title;
    private String description;
    private Map<String, Object> details;
    private String status; // "PENDING", "CONFIRMED", "CANCELLED"
    private String price;
}
