package com.jaba.p2_t.pbxmodels;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CallGroup {

    @Id
    private String id;

    @Column(name = "members")
    private List<String> members;

    @Column(name = "context")
    private String context = "default";

    @Column(name = "voiceMessage")
    private String voiceMessage;

    @Column(name = "strategy")
    private String strategy;
}
