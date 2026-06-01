package com.example.customer.controller;

import com.example.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final CustomerService customerService;

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            Model model
    ) {
        var customersPage = (q != null && !q.isBlank())
                ? customerService.search(q, page, size)
                : customerService.findAll(page, size, "name");

        model.addAttribute("customers", customersPage.content());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customersPage.totalPages());
        model.addAttribute("query", q);
        return "index";
    }

    @GetMapping("/add")
    public String addForm() {
        return "customer-form";
    }
}
