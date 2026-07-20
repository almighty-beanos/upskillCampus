package com.example.omapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;


    @ManyToOne
    @JoinColumn(name="sender_customer_id")
    private Customer senderCustomer;


    @ManyToOne
    @JoinColumn(name="receiver_merchant_id")
    private Merchant receiverMerchant;


    @ManyToOne
    @JoinColumn(name="goods_id")
    private Goods goods;


    private Integer quantity = 1;


    private LocalDateTime transactionDate = LocalDateTime.now();

}