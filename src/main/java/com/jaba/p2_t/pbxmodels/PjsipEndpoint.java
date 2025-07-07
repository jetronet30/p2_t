package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ps_endpoints")
public class PjsipEndpoint {

    @Id
    private String id;

    private String transport;

    private String context;

    private String disallow;

    private String allow;

    @Column(name = "callerid")
    private String callerId;

    private String type;

    private String shower;

    @Column(name = "direct_media")
    private Boolean directMedia;

    @Column(name = "dtmf_mode")
    private String dtmfMode;

    @Column(name = "auth")
    private String authId;

    @Column(name = "aors")
    private String aorsId;

    @Column(name = "mailboxes")
    private String mailboxes;
    @Column(name = "conn_ip") // ახალი ველი IP მისამართისთვის
    private String connIp;
}
