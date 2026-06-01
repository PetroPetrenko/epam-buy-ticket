package com.example.customer.agent.skills;

import com.example.customer.dto.BookingProposal;
import com.example.customer.service.EmailService;
import com.example.customer.service.ProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightBookingSkill implements AgentSkill<FlightBookingSkill.Input, FlightBookingSkill.Output> {

    private final EmailService emailService;
    private final ProposalService proposalService;

    public static class Input {
        private final String from;
        private final String to;
        private final String date;
        private final String passengerName;
        private final String email;
        private final String phone;
        private final String budget;

        public Input(String from, String to, String date, String passengerName, String email, String phone, String budget) {
            this.from = from;
            this.to = to;
            this.date = date;
            this.passengerName = passengerName;
            this.email = email;
            this.phone = phone;
            this.budget = budget;
        }

        public String from() { return from; }
        public String to() { return to; }
        public String date() { return date; }
        public String passengerName() { return passengerName; }
        public String email() { return email; }
        public String phone() { return phone; }
        public String budget() { return budget; }
    }

    public static class Output {
        private final String bookingId;
        private final String status;
        private final String details;

        public Output(String bookingId, String status, String details) {
            this.bookingId = bookingId;
            this.status = status;
            this.details = details;
        }

        public String bookingId() { return bookingId; }
        public String status() { return status; }
        public String details() { return details; }
    }

    @Override
    public String getName() {
        return "bookFlight";
    }

    @Override
    public String getDescription() {
        return "Search and booking of flight tickets between cities for the specified date";
    }

    @Override
    public Class<Input> getInputType() {
        return Input.class;
    }

    @Override
    public Output apply(Input input) {
        log.info("Executing FlightBookingSkill: {}", input);
        
        String bookingId = "FL-" + System.currentTimeMillis() % 10000;
        String price = input.budget() != null ? "$" + input.budget() : "$450";

        // Register the proposal in the interface (Booking Planner)
        proposalService.addProposal(BookingProposal.builder()
                .type("FLIGHT")
                .title("Flight: " + input.from() + " -> " + input.to())
                .description("Passenger: " + input.passengerName() + ", Date: " + input.date() + (input.budget() != null ? " (Within budget: $" + input.budget() + ")" : ""))
                .price(price)
                .details(Map.of(
                    "from", input.from(),
                    "to", input.to(),
                    "date", input.date(),
                    "passengerName", input.passengerName(),
                    "email", input.email(),
                    "bookingId", bookingId,
                    "budget", input.budget() != null ? input.budget() : "not specified"
                ))
                .build());

        return new Output(
            bookingId,
            "PENDING",
            "Flight from " + input.from() + " to " + input.to() + " on " + input.date() + " for " + input.passengerName() + ". Awaiting confirmation in Booking Planner."
        );
    }
}
