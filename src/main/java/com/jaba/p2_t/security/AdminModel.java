package com.jaba.p2_t.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminModel {
    @Id
    private Long id;
    @Column(name = "userName")
    private String userName;
    @Column(name = "userPassword")
    private String userPassword;
    @Column(name = "role")
    private String role;
}
