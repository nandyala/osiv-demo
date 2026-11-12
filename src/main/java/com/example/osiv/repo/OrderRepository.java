package com.example.osiv.repo;

import com.example.osiv.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // FIX option 1: JPQL join fetch, loading orders + customers + items in ONE query
    @Query("select distinct o from PurchaseOrder o join fetch o.customer join fetch o.items order by o.id")
    List<PurchaseOrder> findAllWithCustomerAndItems();

    // FIX option 2: @EntityGraph, same effect without writing JPQL
    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<PurchaseOrder> findWithDetailsById(Long id);
}
