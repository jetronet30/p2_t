package com.jaba.p2_t.pbxmodels;

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
public class OutBoundRouteModel {
    @Id
    private String id;
    @Column(name = "prefix")
    private String prefix;
    @Column(name = "autoAdd")
    private String autoAdd;
    @Column(name = "digits")
    private String digits;

}
