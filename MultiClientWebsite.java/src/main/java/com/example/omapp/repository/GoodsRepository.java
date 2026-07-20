package com.example.omapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.omapp.entity.Goods;

public interface GoodsRepository extends JpaRepository<Goods, Integer> {

    /**
     * Optimized fetch that retrieves the Goods entity alongside its associated 
     * Merchant data in a single SQL JOIN query, preventing N+1 selection issues.
     */
    @Query("SELECT g FROM Goods g JOIN FETCH g.merchant WHERE g.goodsId = :id")
    Optional<Goods> findByIdWithMerchant(@Param("id") Integer id);

    /**
     * Optimized fetch to retrieve all goods along with their merchant details.
     */
    @Query("SELECT g FROM Goods g JOIN FETCH g.merchant")
    List<Goods> findAllWithMerchants();

    /** Used by the merchant catalog to list only this merchant's own goods. */
    List<Goods> findByMerchant_MerchantId(Integer merchantId);
}
