package com.example.osiv.config;

import com.example.osiv.domain.Customer;
import com.example.osiv.domain.PurchaseOrder;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Seeds 20 customers and 50 orders with 3 items each. */
@Component
public class DataLoader implements ApplicationRunner {

    public static final int CUSTOMERS = 20;
    public static final int ORDERS = 50;

    private final EntityManager em;

    public DataLoader(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Customer[] customers = new Customer[CUSTOMERS];
        for (int c = 0; c < CUSTOMERS; c++) {
            customers[c] = new Customer("Customer " + (c + 1));
            em.persist(customers[c]);
        }
        for (int o = 1; o <= ORDERS; o++) {
            PurchaseOrder order = new PurchaseOrder("PO-" + (1000 + o), customers[o % CUSTOMERS]);
            order.addItem("Laptop", 1, new BigDecimal("999.00"));
            order.addItem("Mouse", 2, new BigDecimal("25.50"));
            order.addItem("Monitor", 1, new BigDecimal("199.99"));
            em.persist(order);
        }
    }
}
