package com.jaba.p2_t.pbxmodels;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ps_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PjsipRegistration {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "outbound_auth", nullable = false)
    private String outboundAuth;

    @Column(name = "server_uri", nullable = false)
    private String serverUri;

    @Column(name = "client_uri", nullable = false)
    private String clientUri;

    @Column(name = "retry_interval", nullable = false)
    private Integer retryInterval;

    @Column(name = "forbidden_retry_interval", nullable = false)
    private Integer forbiddenRetryInterval;

    @Column(name = "expiration", nullable = false)
    private Integer expiration;

    @Column(name = "transport", nullable = false)
    private String transport;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "line", nullable = false)
    private Boolean line;

    @Column(name = "support_path", nullable = false)
    private Boolean supportPath;
}
