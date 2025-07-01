package com.jaba.p2_t;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresTestCON {

    private final JdbcTemplate jdbcTemplate;

    public PostgresTestCON(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean testConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            System.out.println("Postgres connection is UP");
            return true;
        } catch (Exception e) {
            System.err.println("Postgres connection failed: " + e.getMessage());
            return false;
        }
    }
}
