package com.example.osiv;

import com.example.osiv.config.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** OSIV ON (Spring Boot default): the bad endpoint "works" but hides an N+1 problem. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:osiv_on")
@AutoConfigureMockMvc
@ActiveProfiles("osiv-on")
class OsivOnTest {

    @Autowired MockMvc mvc;
    @Autowired EntityManagerFactory emf;

    @Test
    void badEndpoint_returns200_butRunsHiddenNPlusOneQueries() throws Exception {
        long queries = QueryCounter.count(emf, () ->
                mvc.perform(get("/api/bad/orders"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(DataLoader.ORDERS)));

        System.out.printf("%n[OSIV ON ] /api/bad/orders  -> %d SQL statements%n%n", queries);

        // 1 (orders) + 20 (one per customer) + 50 (one per order's items) = 71
        assertThat(queries).isEqualTo(1 + DataLoader.CUSTOMERS + DataLoader.ORDERS);
    }

    @Test
    void goodEndpoint_runsOneQuery() throws Exception {
        long queries = QueryCounter.count(emf, () ->
                mvc.perform(get("/api/good/orders"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(DataLoader.ORDERS)));

        System.out.printf("%n[OSIV ON ] /api/good/orders -> %d SQL statement(s)%n%n", queries);
        assertThat(queries).isEqualTo(1);
    }
}
