package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ps_aors")
public class PjsipAor {

    @Id
    private String id;

    @Column(name = "max_contacts")
    private Integer maxContacts;

    @Column(name = "remove_existing")
    private Boolean removeExisting;
    @Column(name = "contact")
    private String contact;
}
