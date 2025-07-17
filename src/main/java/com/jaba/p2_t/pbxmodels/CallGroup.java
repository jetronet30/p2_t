package com.jaba.p2_t.pbxmodels;

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

    @Column(name = "member1")
    private String member1;

    @Column(name = "member2")
    private String member2;

    @Column(name = "member3")
    private String member3;

    @Column(name = "member4")
    private String member4;

    @Column(name = "member5")
    private String member5;

    @Column(name = "member6")
    private String member6;

    @Column(name = "member7")
    private String member7;

    @Column(name = "member8")
    private String member8;

    @Column(name = "member9")
    private String member9;

    @Column(name = "member10")
    private String member10;

    @Column(name = "context")
    private String context = "default";
}
