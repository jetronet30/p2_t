package com.jaba.p2_t.servermanager;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SerInitializer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(8090);
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/p2_t");
        ds.setUsername("pbxa");

        ds.setPassword("pbxA");

        ds.setDriverClassName("org.postgresql.Driver");

        return ds;
    }
}
