package com.example.osiv.web;

import com.example.osiv.domain.PurchaseOrder;
import com.example.osiv.service.OrderDto;
import com.example.osiv.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** PROBLEM: returns JPA entities straight to the client. */
    @GetMapping("/bad/orders")
    public List<PurchaseOrder> badOrders() {
        return orderService.findAllEntities();
    }

    /** FIX: returns DTOs built inside the transaction with a join fetch. */
    @GetMapping("/good/orders")
    public List<OrderDto> goodOrders() {
        return orderService.findAllDtos();
    }

    /** FIX using @EntityGraph. */
    @GetMapping("/good/orders/{id}")
    public OrderDto goodOrder(@PathVariable Long id) {
        return orderService.findDto(id);
    }
}
