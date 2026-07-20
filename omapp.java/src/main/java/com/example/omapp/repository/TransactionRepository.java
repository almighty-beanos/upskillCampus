package com.example.omapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.omapp.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByReceiverMerchant_MerchantId(Integer merchantId);
    List<Transaction> findBySenderCustomer_CustomerIdOrderByTransactionDateDesc(Integer customerId);
}