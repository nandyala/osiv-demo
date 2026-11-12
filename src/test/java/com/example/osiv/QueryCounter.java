package com.example.osiv;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/** Counts SQL statements run while an action executes. */
final class QueryCounter {

    private QueryCounter() {
    }

    interface Action {
        void run() throws Exception;
    }

    static long count(EntityManagerFactory emf, Action action) throws Exception {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        long before = stats.getPrepareStatementCount();
        action.run();
        return stats.getPrepareStatementCount() - before;
    }
}
