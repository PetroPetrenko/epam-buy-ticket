package com.example.customer.service;

import com.example.customer.dto.BookingProposal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class ProposalService {
    private final List<BookingProposal> proposals = new CopyOnWriteArrayList<>();

    public void addProposal(BookingProposal proposal) {
        log.info("Adding new proposal: {}", proposal.getTitle());
        if (proposal.getId() == null) {
            proposal.setId(java.util.UUID.randomUUID().toString());
        }
        proposal.setStatus("PENDING");
        proposals.add(0, proposal);
    }

    public List<BookingProposal> getActiveProposals() {
        return proposals.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .toList();
    }

    public List<BookingProposal> getAllProposals() {
        return proposals;
    }

    public BookingProposal getById(String id) {
        return proposals.stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    public void confirmProposal(String id) {
        getById(id).setStatus("CONFIRMED");
    }

    public void cancelProposal(String id) {
        getById(id).setStatus("CANCELLED");
    }

    public void clearProposals() {
        proposals.clear();
    }
}
