package com.example.osiv.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs how many SQL statements each /api request ran, including the ones
 * Jackson triggers while serializing the response.
 * Demo only: uses global Hibernate statistics, so counts are exact only for one request at a time.
 */
@Component
public class QueryCountFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(QueryCountFilter.class);
    private final Statistics stats;

    public QueryCountFilter(EntityManagerFactory emf) {
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long before = stats.getPrepareStatementCount();
        try {
            chain.doFilter(req, res);
        } finally {
            long count = stats.getPrepareStatementCount() - before;
            log.info(">>> {} {} -> status {}, {} SQL statement(s)",
                    req.getMethod(), req.getRequestURI(), res.getStatus(), count);
        }
    }
}
