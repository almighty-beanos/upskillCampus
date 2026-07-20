// Defining the procedural queries required to update DB with business logic (Merchants.sql)
package com.example.omapp.repository;

import com.example.omapp.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Integer> {

    Optional<Merchant> findByEmail(String email);

    boolean existsByEmail(String email);
}