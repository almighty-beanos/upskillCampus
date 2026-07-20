package com.example.omapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.omapp.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByMerchant_MerchantId(Integer merchantId);

    List<Appointment> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);
}