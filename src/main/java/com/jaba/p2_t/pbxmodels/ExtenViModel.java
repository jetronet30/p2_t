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
@Table(name = "ExtenViModel")
public class ExtenViModel {

    @Id
    private String id;
    @Column(name = "displayName")
    private String displayName;
    @Column(name = "virPass")
    private String virPass;
    @Column(name = "virContext")
    private String virContext;
    @Column(name = "virUsIp")
    private String virUsIp;
    @Column(name = "outPermit")
    private int outPermit = 3;


    @Column(name = "active")///////////////
    private boolean active;////////////////
    @Column(name = "modelName")
    private String modelName;


}
