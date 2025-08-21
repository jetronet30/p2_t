package com.jaba.p2_t.models;

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
public class ServiceCodeModel {

    @Id
    private int id;
    @Column(name = "clean")
    private String clean;

    @Column(name = "dirty")
    private String dirty;

    @Column(name = "outOfOrder")
    private String outOfOrder;

    @Column(name = "outOfService")
    private String outOfService;

    @Column(name = "inspected")
    private String  inspected;
    
    @Column(name = "alarm")
    private String alarm;


}
