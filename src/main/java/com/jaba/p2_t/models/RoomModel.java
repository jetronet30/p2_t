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
public class RoomModel {
    @Id
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "status")
    private String status;

}
