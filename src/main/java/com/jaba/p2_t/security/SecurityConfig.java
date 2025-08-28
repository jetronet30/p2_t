package com.jaba.p2_t.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF გამორთულია მხოლოდ SSE და POST /rooms-ზე
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // სტატიკური რესურსები ყველასთვის
                        .requestMatchers("/styles/**", "/scripts/**", "/images/**").permitAll()
                        // SSE ყველასთვის
                        .requestMatchers("/rooms/subscribe").permitAll()
                        // POST /rooms ავტორიზაცია შესაძლებელია ან permitAll თუ არ არის აუცილებელი
                        .requestMatchers("/rooms").permitAll()
                        // მთავარი გვერდი მხოლოდ USER როლისთვის
                        .requestMatchers("/home").hasRole("USER")
                        // roomstatus
                        .requestMatchers("/roomstatus/**").permitAll()
                        // დანარჩენი მოთხოვნები ავტორიზაცია
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .logout(logout -> logout.permitAll());

        // უსაფრთხოების სათაური არ გამოაჩინოს
        http.headers(headers -> headers.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
