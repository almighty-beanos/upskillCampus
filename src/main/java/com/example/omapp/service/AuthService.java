package com.example.omapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.omapp.dto.LoginRequest;
import com.example.omapp.dto.SignupRequest;
import com.example.omapp.entity.Customer;
import com.example.omapp.entity.Merchant;
import com.example.omapp.repository.CustomerRepository;
import com.example.omapp.repository.MerchantRepository;
import com.example.omapp.security.Role;

/**
 * Service handling authentication workflows including registration and login
 * for both Customers and Merchants.
 */
@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public AuthService(CustomerRepository customerRepository, 
                       MerchantRepository merchantRepository, 
                       PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user based on their specified role (CUSTOMER or MERCHANT).
     * Duplicate emails are prevented within the same role/table.
     *
     * @param request the signup details
     * @throws RuntimeException if the email is taken or the role is invalid
     */
    public void register(SignupRequest request) {
        
        // --- CUSTOMER REGISTRATION FLOW ---
        if (request.getRole() == Role.CUSTOMER) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Customer email already exists");
            }

            Customer customer = new Customer();
            customer.setCustomerName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            
            // Encrypt plaintext password to BCrypt hash before saving
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
            
            customerRepository.save(customer);
            return;
        }

        // --- MERCHANT REGISTRATION FLOW ---
        if (request.getRole() == Role.MERCHANT) {
            if (merchantRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Merchant email already exists");
            }

            Merchant merchant = new Merchant();
            merchant.setMerchantName(request.getName());
            merchant.setEmail(request.getEmail());
            merchant.setPhone(request.getPhone());
            
            // Encrypt plaintext password to BCrypt hash before saving
            merchant.setPassword(passwordEncoder.encode(request.getPassword()));
            
            merchantRepository.save(merchant);
            return;
        }

        throw new RuntimeException("Invalid user role");
    }

    /**
     * Validates credentials for a logging-in user.
     *
     * @param request the login credentials and requested role
     * @return the successfully authenticated Role
     * @throws RuntimeException if the user is not found or the password is invalid
     */
    public Role login(LoginRequest request) {
        
        // --- CUSTOMER LOGIN FLOW ---
        if (request.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Compare entered plaintext password against the stored BCrypt hash
            if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                throw new RuntimeException("Invalid password");
            }
            return Role.CUSTOMER;
        }

        // --- MERCHANT LOGIN FLOW ---
        if (request.getRole() == Role.MERCHANT) {
            Merchant merchant = merchantRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Merchant not found"));

            // Compare entered plaintext password against the stored BCrypt hash
            if (!passwordEncoder.matches(request.getPassword(), merchant.getPassword())) {
                throw new RuntimeException("Invalid password");
            }
            return Role.MERCHANT;
        }

        throw new RuntimeException("Invalid role");
    }
}