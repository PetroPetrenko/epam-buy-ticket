package com.example.customer.repository;

import com.example.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Customer> search(@Param("q") String query, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:lastId IS NULL OR c.id > :lastId) " +
           "ORDER BY c.id ASC")
    List<Customer> findAllSeek(@Param("lastId") Long lastId, Pageable pageable);
}
