package com.jaba.p2_t.pbxmodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ps_contacts")
public class PjsipContact {

    @Id
    @Column(length = 255)
    private String id;

    @Column(name = "uri")
    private String uri;

    @Column(name = "expiration_time")
    private Long expirationTime;

    @Column(name = "qualify_frequency")
    private Integer qualifyFrequency;

    @Column(name = "outbound_proxy")
    private String outboundProxy;

    @Column(name = "path")
    private String path;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "reg_server")
    private String regServer;

    @Column(name = "authenticate_qualify")
    private String authenticateQualify;

    @Column(name = "via_addr")
    private String viaAddr;

    @Column(name = "via_port")
    private Integer viaPort;

    @Column(name = "call_id")
    private String callId;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "prune_on_boot")
    private Boolean pruneOnBoot;

    @Column(name = "network_ip")
    private String networkIp;

    @Column(name = "network_port")
    private Integer networkPort;

    @Column(name = "qualify_timeout")
    private Float qualifyTimeout;

    @Column(name = "qualify_2xx_only")
    private Boolean qualify2xxOnly;

}
