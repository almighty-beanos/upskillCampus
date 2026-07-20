package com.example.omapp.service;

import com.example.omapp.dto.AppointmentBookingRequest;
import com.example.omapp.dto.CheckoutGoods;
import com.example.omapp.dto.CheckoutServiceDto;
import com.example.omapp.dto.GoodsPurchaseRequest;
import com.example.omapp.dto.ThankYouDto;
import com.example.omapp.entity.Appointment;
import com.example.omapp.entity.Customer;
import com.example.omapp.entity.Goods;
import com.example.omapp.entity.Merchant;
import com.example.omapp.entity.ServiceEntity;
import com.example.omapp.entity.Transaction;
import com.example.omapp.repository.AppointmentRepository;
import com.example.omapp.repository.CustomerRepository;
import com.example.omapp.repository.GoodsRepository;
import com.example.omapp.repository.MerchantRepository;
import com.example.omapp.repository.ServiceRepository;
import com.example.omapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class CheckoutService {

    private static final String DEFAULT_PHOTO = "/assets/NoPhoto.png";

    private final GoodsRepository goodsRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;

    public CheckoutService(GoodsRepository goodsRepository,
                            ServiceRepository serviceRepository,
                            CustomerRepository customerRepository,
                            MerchantRepository merchantRepository,
                            TransactionRepository transactionRepository,
                            AppointmentRepository appointmentRepository) {
        this.goodsRepository = goodsRepository;
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ---------- display ----------

    public CheckoutGoods getGoodsForCheckout(Integer goodsId) {
        Goods goods = goodsRepository.findByIdWithMerchant(goodsId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toCheckoutGoodsDto(goods);
    }

    public CheckoutServiceDto getServiceForCheckout(Integer serviceId) {
        ServiceEntity service = serviceRepository.findByIdWithMerchant(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return toCheckoutServiceDto(service);
    }

    // ---------- goods purchase ----------

    @Transactional
    public Integer purchaseGoods(String customerEmail, Integer goodsId, GoodsPurchaseRequest request) {
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Goods goods = goodsRepository.findByIdWithMerchant(goodsId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Merchant merchant = goods.getMerchant();

        BigDecimal total = goods.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        transferWallet(customer, merchant, total);

        Transaction transaction = new Transaction();
        transaction.setSenderCustomer(customer);
        transaction.setReceiverMerchant(merchant);
        transaction.setGoods(goods);
        transaction.setQuantity(request.getQuantity());
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction).getTransactionId();
    }

    // ---------- appointment booking ----------

    @Transactional
    public Integer bookAppointment(String customerEmail, Integer serviceId, AppointmentBookingRequest request) {
        if (request.getAppointmentStartDate() == null || request.getAppointmentEndDate() == null
                || request.getStartingTime() == null || request.getWorkingHours() == null) {
            throw new RuntimeException("Please fill in all booking details");
        }
        if (request.getAppointmentEndDate().isBefore(request.getAppointmentStartDate())) {
            throw new RuntimeException("End date cannot be before the start date");
        }
        if (request.getWorkingHours() < 1 || request.getWorkingHours() > 24) {
            throw new RuntimeException("Working hours must be between 1 and 24");
        }

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        ServiceEntity service = serviceRepository.findByIdWithMerchant(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Merchant merchant = service.getMerchant();

        BigDecimal total = computeAppointmentTotal(service.getPricePerHour(),
                request.getAppointmentStartDate(), request.getAppointmentEndDate(), request.getWorkingHours());

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            // Same formula used on the merchant sales dashboard: (endDate - startDate in
            // days) * workingHours * pricePerHour. A same-day booking yields 0 days, which
            // would otherwise be a free appointment — rejected here instead of allowing it.
            throw new RuntimeException("Appointment must span at least one full day");
        }

        transferWallet(customer, merchant, total);

        Appointment appointment = new Appointment();
        appointment.setService(service);
        appointment.setMerchant(merchant);
        appointment.setCustomer(customer);
        appointment.setAppointmentStartDate(request.getAppointmentStartDate());
        appointment.setAppointmentEndDate(request.getAppointmentEndDate());
        appointment.setStartingTime(request.getStartingTime());
        appointment.setWorkingHours(request.getWorkingHours());
        appointment.setCreatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment).getAppointmentId();
    }

    // ---------- thank-you lookups (ownership-checked) ----------

    public ThankYouDto getTransactionThankYou(Integer transactionId, String customerEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getSenderCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            throw new RuntimeException("You do not have permission to view this transaction");
        }

        Merchant merchant = transaction.getReceiverMerchant();
        BigDecimal total = transaction.getGoods().getPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()));

        return ThankYouDto.builder()
                .type("GOODS")
                .referenceId(transaction.getTransactionId())
                .itemName(transaction.getGoods().getGoodsName())
                .totalAmount(total)
                .merchantName(merchant.getMerchantName())
                .merchantEmail(merchant.getEmail())
                .merchantPhone(merchant.getPhone())
                .build();
    }

    public ThankYouDto getAppointmentThankYou(Integer appointmentId, String customerEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
            throw new RuntimeException("You do not have permission to view this appointment");
        }

        Merchant merchant = appointment.getMerchant();
        BigDecimal total = computeAppointmentTotal(appointment.getService().getPricePerHour(),
                appointment.getAppointmentStartDate(), appointment.getAppointmentEndDate(), appointment.getWorkingHours());

        return ThankYouDto.builder()
                .type("SERVICE")
                .referenceId(appointment.getAppointmentId())
                .itemName(appointment.getService().getServiceCategory())
                .totalAmount(total)
                .merchantName(merchant.getMerchantName())
                .merchantEmail(merchant.getEmail())
                .merchantPhone(merchant.getPhone())
                .build();
    }

    // ---------- helpers ----------

    private void transferWallet(Customer customer, Merchant merchant, BigDecimal amount) {
        if (customer.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance for this order");
        }
        customer.setWalletBalance(customer.getWalletBalance().subtract(amount));
        merchant.setWalletBalance(merchant.getWalletBalance().add(amount));
        customerRepository.save(customer);
        merchantRepository.save(merchant);
    }

    private BigDecimal computeAppointmentTotal(BigDecimal pricePerHour,
                                                java.time.LocalDate startDate,
                                                java.time.LocalDate endDate,
                                                Integer workingHours) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return pricePerHour.multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(workingHours));
    }

    private CheckoutGoods toCheckoutGoodsDto(Goods goods) {
        String media = (goods.getMediaUrls() != null && goods.getMediaUrls().length > 0)
                ? goods.getMediaUrls()[0] : DEFAULT_PHOTO;
        return CheckoutGoods.builder()
                .goodsId(goods.getGoodsId())
                .goodsName(goods.getGoodsName())
                .goodsDesc(goods.getGoodsDesc())
                .goodsCategory(goods.getGoodsCategory())
                .price(goods.getPrice())
                .merchantName(goods.getMerchant().getMerchantName())
                .mediaUrl(media)
                .build();
    }

    private CheckoutServiceDto toCheckoutServiceDto(ServiceEntity service) {
        String media = (service.getMediaUrls() != null && service.getMediaUrls().length > 0)
                ? service.getMediaUrls()[0] : DEFAULT_PHOTO;
        return CheckoutServiceDto.builder()
                .serviceId(service.getServiceId())
                .serviceCategory(service.getServiceCategory())
                .serviceDesc(service.getServiceDesc())
                .pricePerHour(service.getPricePerHour())
                .availabilityStartTime(service.getAvailabilityStartTime())
                .availabilityEndTime(service.getAvailabilityEndTime())
                .merchantName(service.getMerchant().getMerchantName())
                .mediaUrl(media)
                .build();
    }
}