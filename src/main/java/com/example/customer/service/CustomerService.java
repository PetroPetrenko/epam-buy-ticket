package com.example.customer.service;

import com.example.customer.dto.CustomerDto;
import com.example.customer.entity.Customer;
import com.example.customer.exception.CustomerException;
import com.example.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = "customers")
@Slf4j
public class CustomerService {

    private final CustomerRepository repo;

    public List<CustomerDto.AuditResponse> getAuditHistory(Long id) {
        log.debug("Fetching audit history for customer id: {}", id);
        return List.of(); // Envers disabled for local testing
    }

    @Cacheable(key = "{'all', #page, #size, #sort}")
    public CustomerDto.PageResponse<CustomerDto.Response> findAll(int page, int size, String sort) {
        log.debug("Fetching all customers (Offset-based): page={}, size={}, sort={}", page, size, sort);
        var pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CustomerDto.Response> result = repo.findAll(pageable)
                .map(CustomerDto.Response::from);
        return toPageResponse(result);
    }

    @Cacheable(key = "{'seek', #lastId, #size}")
    public CustomerDto.SeekResponse<CustomerDto.Response> findAllSeek(Long lastId, int size) {
        log.debug("Fetching customers (Seek-based): lastId={}, size={}", lastId, size);
        var pageable = PageRequest.of(0, size + 1);
        List<Customer> customers = repo.findAllSeek(lastId, pageable);

        boolean hasNext = customers.size() > size;
        List<Customer> resultList = hasNext ? customers.subList(0, size) : customers;

        List<CustomerDto.Response> content = resultList.stream()
                .map(CustomerDto.Response::from)
                .toList();

        Long newLastId = content.isEmpty() ? null : content.get(content.size() - 1).id();

        return new CustomerDto.SeekResponse<>(content, newLastId, hasNext);
    }

    @Cacheable(key = "#id")
    public CustomerDto.Response findById(Long id) {
        log.debug("Finding customer by id: {}", id);
        return repo.findById(id)
                .map(CustomerDto.Response::from)
                .orElseThrow(() -> new CustomerException.NotFound(id));
    }

    @Cacheable(key = "{'search', #keyword, #page, #size}")
    public CustomerDto.PageResponse<CustomerDto.Response> search(String keyword, int page, int size) {
        log.debug("Searching customers: keyword='{}', page={}, size={}", keyword, page, size);
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<CustomerDto.Response> result = repo.search(keyword, pageable)
                .map(CustomerDto.Response::from);
        return toPageResponse(result);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public CustomerDto.Response create(CustomerDto.CreateRequest req) {
        log.info("Creating new customer with email: {}", req.email());
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new CustomerException.EmailAlreadyExists(req.email());
        }
        Customer customer = new Customer(req.name(), req.email(), req.address());
        customer.setPhone(req.phone());
        customer.setNotes(req.notes());
        Customer saved = repo.save(customer);
        return CustomerDto.Response.from(saved);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public CustomerDto.Response update(Long id, CustomerDto.UpdateRequest req) {
        log.info("Updating customer with id: {}", id);
        Customer customer = repo.findById(id)
                .orElseThrow(() -> new CustomerException.NotFound(id));

        if (repo.existsByEmailAndIdNot(req.email(), id)) {
            throw new CustomerException.EmailAlreadyExists(req.email());
        }

        customer.setName(req.name());
        customer.setEmail(req.email());
        customer.setPhone(req.phone());
        customer.setAddress(req.address());
        customer.setNotes(req.notes());

        Customer saved = repo.save(customer);
        return CustomerDto.Response.from(saved);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        log.warn("Deleting customer with id: {}", id);
        if (!repo.existsById(id)) {
            throw new CustomerException.NotFound(id);
        }
        repo.deleteById(id);
    }

    private <T> CustomerDto.PageResponse<T> toPageResponse(Page<T> page) {
        return new CustomerDto.PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
