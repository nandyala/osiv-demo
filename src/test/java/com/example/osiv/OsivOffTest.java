package com.example.osiv;

import com.example.osiv.config.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** OSIV OFF (recommended): the bad endpoint fails fast, the fixed endpoint works with 1 query. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:osiv_off")
@AutoConfigureMockMvc
class OsivOffTest {

    @Autowired MockMvc mvc;
    @Autowired EntityManagerFactory emf;

    @Test
    void badEndpoint_failsWithLazyInitializationException() throws Exception {
        mvc.perform(get("/api/bad/orders"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> {
                    Throwable root = result.getResolvedException();
                    while (root != null && root.getCause() != null) {
                        root = root.getCause();
                    }
                    System.out.printf("%n[OSIV OFF] /api/bad/orders  -> 500, root cause: %s%n%n", root);
                    assertThat(root).isInstanceOf(LazyInitializationException.class);
                });
    }

    @Test
    void goodEndpoint_runsOneQuery() throws Exception {
        long queries = QueryCounter.count(emf, () ->
                mvc.perform(get("/api/good/orders"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(DataLoader.ORDERS))
                        .andExpect(jsonPath("$[0].customerName").exists())
                        .andExpect(jsonPath("$[0].items.length()").value(3)));

        System.out.printf("%n[OSIV OFF] /api/good/orders -> %d SQL statement(s)%n%n", queries);
        assertThat(queries).isEqualTo(1);
    }

    @Test
    void entityGraphEndpoint_runsOneQuery() throws Exception {
        long queries = QueryCounter.count(emf, () ->
                mvc.perform(get("/api/good/orders/{id}", 1 + DataLoader.CUSTOMERS))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items.length()").value(3)));

        System.out.printf("%n[OSIV OFF] /api/good/orders/{id} (@EntityGraph) -> %d SQL statement(s)%n%n", queries);
        assertThat(queries).isEqualTo(1);
    }
}
