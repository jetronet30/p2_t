package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ps_auths")
public class PjsipAuth {

    @Id
    private String id;

    @Column(name = "auth_type")
    private String authType;

    private String password;

    private String username;
}
