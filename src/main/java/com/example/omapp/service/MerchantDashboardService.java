package com.example.omapp.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.omapp.dto.AppointmentSales;
import com.example.omapp.dto.MerchantProfile;
import com.example.omapp.dto.TransactionSales;
import com.example.omapp.dto.UpdateMerchantProfileRequest;
import com.example.omapp.entity.Appointment;
import com.example.omapp.entity.Merchant;
import com.example.omapp.entity.Transaction;
import com.example.omapp.repository.AppointmentRepository;
import com.example.omapp.repository.MerchantRepository;
import com.example.omapp.repository.TransactionRepository;

@Service
public class MerchantDashboardService {

    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public MerchantDashboardService(MerchantRepository merchantRepository,
                                     TransactionRepository transactionRepository,
                                     AppointmentRepository appointmentRepository,
                                     PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Merchant getMerchantByEmail(String email) {
        return merchantRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
    }

    public MerchantProfile getProfile(Merchant merchant) {
        return MerchantProfile.builder()
                .merchantId(merchant.getMerchantId())
                .merchantName(merchant.getMerchantName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .createdAt(merchant.getCreatedAt())
                .walletBalance(merchant.getWalletBalance())
                .build();
    }

    public List<TransactionSales> getTransactions(Integer merchantId) {
        return transactionRepository.findByReceiverMerchant_MerchantId(merchantId).stream()
                .map(this::toTransaction)
                .collect(Collectors.toList());
    }

    public List<AppointmentSales> getAppointments(Integer merchantId) {
        return appointmentRepository.findByMerchant_MerchantId(merchantId).stream()
                .map(this::toAppointmentDto)
                .collect(Collectors.toList());
    }

    private TransactionSales toTransaction(Transaction transaction) {
        BigDecimal unitPrice = transaction.getGoods().getPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(transaction.getQuantity()));

        return TransactionSales.builder()
                .transactionId(transaction.getTransactionId())
                .goodsName(transaction.getGoods().getGoodsName())
                .goodsCategory(transaction.getGoods().getGoodsCategory())
                .quantity(transaction.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(total)
                .customerName(transaction.getSenderCustomer() != null
                        ? transaction.getSenderCustomer().getCustomerName() : "Unknown")
                .merchantName(transaction.getReceiverMerchant() != null 
                        ? transaction.getReceiverMerchant().getMerchantName() : "Unknown")
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    private AppointmentSales toAppointmentDto(Appointment appointment) {
        BigDecimal pricePerHour = appointment.getService().getPricePerHour();
        
        // Fixed: Inclusive day count calculation so same-day appointments don't result in 0
        long days = ChronoUnit.DAYS.between(appointment.getAppointmentStartDate(), appointment.getAppointmentEndDate()) + 1;

        BigDecimal total = pricePerHour
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(appointment.getWorkingHours()));

        return AppointmentSales.builder()
                .appointmentId(appointment.getAppointmentId())
                .serviceCategory(appointment.getService().getServiceCategory())
                .customerName(appointment.getCustomer() != null
                        ? appointment.getCustomer().getCustomerName() : "Unknown")
                .merchantName(appointment.getMerchant() != null // Assuming your relationship exists
                        ? appointment.getMerchant().getMerchantName() : "Unknown")
                .appointmentStartDate(appointment.getAppointmentStartDate())
                .appointmentEndDate(appointment.getAppointmentEndDate())
                .startingTime(appointment.getStartingTime())
                .workingHours(appointment.getWorkingHours())
                .pricePerHour(pricePerHour)
                .totalAmount(total)
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    /**
     * Applies a profile update in place on the given (already-loaded) Merchant and persists it.
     * Always requires the current password; email uniqueness and new-password confirmation
     * are only checked when those fields are actually being changed.
     */
    @Transactional
    public void updateProfile(Merchant merchant, UpdateMerchantProfileRequest request) {

        if (request.getCurrentPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), merchant.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(merchant.getEmail())
                && merchantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        merchant.setMerchantName(request.getMerchantName());
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                throw new RuntimeException("New password and confirmation do not match");
            }
            merchant.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        merchantRepository.save(merchant);
    }
}