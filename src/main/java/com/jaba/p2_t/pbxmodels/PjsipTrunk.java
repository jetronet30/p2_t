package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pjsip_trunks")
public class PjsipTrunk {

    @Id
    private String id;  // ტრანკის სახელი, eg: "telasi_trunk"

    private String username;

    private String password;

    @Column(name = "contact_uri")
    private String contactUri;  // eg: sip:sip.provider.com

    @Column(name = "outbound_auth")
    private String outboundAuth;

    @Column(name = "transport")
    private String transport = "udp";

    @Column(name = "callerid")
    private String callerId;

    @Column(name = "context")
    private String context = "from-trunk";

    @Column(name = "from_domain")
    private String fromDomain;  // eg: provider.com

    @Column(name = "outbound_proxy")
    private String outboundProxy; // eg: sip:sip.provider.com

    private boolean enabled = true;
}
