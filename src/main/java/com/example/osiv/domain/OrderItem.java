package com.example.osiv.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore // avoid infinite recursion when the bad endpoint serializes entities
    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrder order;

    private String product;
    private int quantity;
    private BigDecimal price;

    protected OrderItem() {
    }

    OrderItem(PurchaseOrder order, String product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() { return id; }
    public PurchaseOrder getOrder() { return order; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}
