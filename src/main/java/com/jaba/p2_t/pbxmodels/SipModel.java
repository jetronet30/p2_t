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
@Table(name = "sip_settings")
@Entity
public class SipModel {
    @Id
    @Column(name = "Id")
    private Long id;
    @Column(name = "transport_protocol")
    private String transportProtocol;
    @Column(name = "bind_address")
    private String bindAddress;
    @Column(name = "sip_udp_port")
    private Integer sipUdpPort;
    @Column(name = "sip_tcp_port")
    private Integer sipTcpPort;
    @Column(name = "sip_tls_port")
    private Integer sipTlsPort;
    @Column(name = "realm")
    private String realm;
    @Column(name = "dtmf_mode")
    private String dtmfMode;
    @Column(name = "default_password")
    private String defPassword;

}
