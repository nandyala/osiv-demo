package com.example.osiv.service;

import com.example.osiv.domain.OrderItem;
import com.example.osiv.domain.PurchaseOrder;

import java.math.BigDecimal;
import java.util.List;

/** What the Angular UI actually receives. No entities leave the service layer. */
public record OrderDto(Long id, String orderNumber, String customerName, List<Item> items) {

    public record Item(String product, int quantity, BigDecimal price) {
        static Item from(OrderItem i) {
            return new Item(i.getProduct(), i.getQuantity(), i.getPrice());
        }
    }

    static OrderDto from(PurchaseOrder o) {
        return new OrderDto(o.getId(), o.getOrderNumber(), o.getCustomer().getName(),
                o.getItems().stream().map(Item::from).toList());
    }
}
