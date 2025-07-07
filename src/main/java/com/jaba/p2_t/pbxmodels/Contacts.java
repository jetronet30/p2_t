package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ps_contacts")
public class Contacts {

    @Id
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
    private Boolean authenticateQualify;

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

    @Column(name = "roundtrip_usec")
    private Long roundtripUsec;

    @Column(name = "qualify_timeout")
    private Double qualifyTimeout;

    @Column(name = "qualify_received")
    private String qualifyReceived;

    @Column(name = "qualify_sent")
    private String qualifySent;

    @Column(name = "read_only")
    private Boolean readOnly;

    @Column(name = "last_update_time")
    private Timestamp lastUpdateTime;
}
