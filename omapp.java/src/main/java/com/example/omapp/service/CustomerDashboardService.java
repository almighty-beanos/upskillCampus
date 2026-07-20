package com.example.omapp.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.omapp.dto.AppointmentSales;
import com.example.omapp.dto.CustomerProfile;
import com.example.omapp.dto.ProfileUpdate;
import com.example.omapp.dto.TransactionSales;
import com.example.omapp.entity.Customer;
import com.example.omapp.repository.AppointmentRepository;
import com.example.omapp.repository.CustomerRepository;
import com.example.omapp.repository.TransactionRepository;

@Service
@Transactional(readOnly = true)
public class CustomerDashboardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    // Explicit constructor for dependency injection
    public CustomerDashboardService(CustomerRepository customerRepository,
                                    TransactionRepository transactionRepository,
                                    AppointmentRepository appointmentRepository,
                                    PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CustomerProfile getProfile(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Changed target from CustomerProfileDTO to CustomerProfile to match your project tree structure
        return new CustomerProfile(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getWalletBalance()
        );
    }

    public List<TransactionSales> getPurchaseHistory(Integer customerId) {
        return transactionRepository.findBySenderCustomer_CustomerIdOrderByTransactionDateDesc(customerId)
                .stream()
                .map(t -> {
                    BigDecimal unitPrice = t.getGoods().getPrice();
                    BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(t.getQuantity()));
                    
                    return TransactionSales.builder()
                            .transactionId(t.getTransactionId())
                            .goodsName(t.getGoods().getGoodsName())
                            .goodsCategory(t.getGoods().getGoodsCategory())
                            .quantity(t.getQuantity())
                            .unitPrice(unitPrice)
                            .totalAmount(total)
                            .customerName(t.getSenderCustomer() != null ? t.getSenderCustomer().getCustomerName() : "N/A")
                            .merchantName(t.getReceiverMerchant() != null ? t.getReceiverMerchant().getMerchantName() : "N/A")
                            .transactionDate(t.getTransactionDate())
                            .build();
                })
                .toList();
    }

    public List<AppointmentSales> getAppointmentHistory(Integer customerId) {
        return appointmentRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(a -> {
                    BigDecimal pricePerHour = a.getService().getPricePerHour();
                    long days = ChronoUnit.DAYS.between(a.getAppointmentStartDate(), a.getAppointmentEndDate());
                    days = days == 0 ? 1 : days;
                    
                    BigDecimal total = pricePerHour
                            .multiply(BigDecimal.valueOf(days))
                            .multiply(BigDecimal.valueOf(a.getWorkingHours()));

                    return AppointmentSales.builder()
                            .appointmentId(a.getAppointmentId())
                            .serviceCategory(a.getService().getServiceCategory())
                            .customerName(a.getCustomer() != null ? a.getCustomer().getCustomerName() : "N/A")
                            .merchantName(a.getMerchant() != null ? a.getMerchant().getMerchantName() : "N/A")
                            .appointmentStartDate(a.getAppointmentStartDate())
                            .appointmentEndDate(a.getAppointmentEndDate())
                            .startingTime(a.getStartingTime())
                            .workingHours(a.getWorkingHours())
                            .pricePerHour(pricePerHour)
                            .totalAmount(total)
                            .createdAt(a.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Transactional
    public void updateProfile(Integer customerId, ProfileUpdate dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), customer.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        customer.setCustomerName(dto.getCustomerName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            customer.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        customerRepository.save(customer);
    }
}