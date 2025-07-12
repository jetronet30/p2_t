package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TrunkViModel")
public class TrunkViModel {

    @Id
    private String id;

    @Column(name = "trunkName")
    private String trunkName;

    @Column(name = "username")
    private String username;


    @Column(name = "outboundProxy")
    private String outboundProxy;

    @Column(name = "registrationEnabled")
    private boolean registrationEnabled;

    @Column(name = "active")
    private boolean active;


}
