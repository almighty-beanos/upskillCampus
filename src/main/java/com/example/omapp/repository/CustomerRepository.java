// Bridges the gap between business logic and database updating
// Can decide how you may interact with the data in the database (Customers.sql)
package com.example.omapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.example.omapp.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<String> findCustomerNameByEmail(@Param("email") String email);
}