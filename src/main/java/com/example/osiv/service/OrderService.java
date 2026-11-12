package com.example.osiv.service;

import com.example.osiv.domain.PurchaseOrder;
import com.example.osiv.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ---------------- PROBLEM ----------------
    // Returns entities. Customers and items are LAZY and are NOT loaded here.
    // They get loaded later, by Jackson, while writing the JSON:
    //   OSIV on  -> works, but runs 1 + N + N hidden queries (N+1)
    //   OSIV off -> LazyInitializationException (HTTP 500)
    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAllEntities() {
        return orderRepository.findAll();
    }

    // ---------------- FIX ----------------
    // Loads everything the response needs in one query, maps to DTOs
    // while the transaction is still open. Works the same with OSIV on or off.
    @Transactional(readOnly = true)
    public List<OrderDto> findAllDtos() {
        return orderRepository.findAllWithCustomerAndItems().stream()
                .map(OrderDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto findDto(Long id) {
        return orderRepository.findWithDetailsById(id)
                .map(OrderDto::from)
                .orElseThrow(() -> new NoSuchElementException("Order " + id));
    }
}
