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

    @Column(name = "conn_ip")
    private String connIp;

    @Column(name = "trust_id_outbound")
    private Boolean trustIdOutbound;

    @Column(name = "from_domain")
    private String fromDomain;

    @Column(name = "rewrite_contact")
    private Boolean rewriteContact;

    @Column(name = "qualify_frequency")
    private Integer qualifyFrequency;

    @Column(name = "rtp_symmetric")
    private Boolean rtpSymmetric;

    @Column(name = "force_rport")
    private Boolean forceRport;

    @Column(name = "from_user")
    private String fromUser;

    @Column(name = "outbound_auth")
    private String outboundAuth;
}
