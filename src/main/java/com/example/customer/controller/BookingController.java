package com.example.customer.controller;

import com.example.customer.dto.BookingProposal;
import com.example.customer.service.EmailService;
import com.example.customer.service.ProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/planner")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final ProposalService proposalService;
    private final EmailService emailService;

    @GetMapping
    public String showPlanner(Model model) {
        model.addAttribute("proposals", proposalService.getActiveProposals());
        return "booking-planner";
    }

    @PostMapping("/confirm/{id}")
    public String confirm(@PathVariable String id) {
        BookingProposal proposal = proposalService.getById(id);
        if (proposal != null) {
            proposalService.confirmProposal(id);
            sendConfirmationEmail(proposal);
            log.info("Proposal confirmed and email sent: {}", id);
        }
        return "redirect:/planner";
    }

    private void sendConfirmationEmail(BookingProposal proposal) {
        String email = (String) proposal.getDetails().get("email");
        if (email == null || email.isBlank()) {
            log.warn("No email found in proposal details for id: {}", proposal.getId());
            return;
        }

        Map<String, Object> emailVars = new HashMap<>(proposal.getDetails());
        emailVars.put("status", "CONFIRMED");

        if ("HOTEL".equals(proposal.getType())) {
            emailService.sendBookingConfirmation(email, "Подтверждение бронирования отеля", "hotel-confirmation", emailVars);
        } else if ("FLIGHT".equals(proposal.getType())) {
            emailService.sendBookingConfirmation(email, "Ваш электронный билет", "flight-confirmation", emailVars);
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable String id) {
        proposalService.cancelProposal(id);
        return "redirect:/planner";
    }
}
