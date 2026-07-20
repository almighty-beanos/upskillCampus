package com.example.omapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.omapp.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {

    /**
     * Optimized fetch that retrieves the Services entity alongside its associated 
     * Merchant data in a single SQL JOIN query, preventing N+1 selection issues.
     */
    @Query("SELECT s FROM ServiceEntity s JOIN FETCH s.merchant WHERE s.serviceId = :id")
    Optional<ServiceEntity> findByIdWithMerchant(@Param("id") Integer id);

    /**
     * Optimized fetch to retrieve all services along with their merchant details.
     */
    @Query("SELECT s FROM ServiceEntity s JOIN FETCH s.merchant")
    List<ServiceEntity> findAllWithMerchants();

    /** Used by the merchant catalog to list only this merchant's own services. */
    List<ServiceEntity> findByMerchant_MerchantId(Integer merchantId);
}
