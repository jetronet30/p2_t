package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaba.p2_t.asteriskmanager.AsteriskManager;
import com.jaba.p2_t.bootinit.RepoInit;
import com.jaba.p2_t.pbxmodels.SipModel;
import com.jaba.p2_t.pbxrepos.SipSettingsRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SipSettingsInit {
    private final AsteriskManager asteriskManager;
    private final SipSettingsRepo sRepo;
    private static final File SIP_SETTINGS = new File(RepoInit.SERVER_RESOURCES, "sip_settings.json");
    private final ObjectMapper mapper = new ObjectMapper();

    private int sipUdpPort;
    private int sipTcpPort;
    private int sipTlsPort;
    private String dtmfMode;
    private String defPassword;
    private String bindAddress;

    // --- Load SIP Settings on Startup ---
    @PostConstruct
    public void initSipSetting() {
        if (!SIP_SETTINGS.exists() || sRepo.count() == 0) {
            try {
                SIP_SETTINGS.createNewFile();
                SipModel sipModel = new SipModel();
                sipModel.setId(1L);
                sipModel.setSipUdpPort(5060);
                sipModel.setSipTcpPort(5070);
                sipModel.setSipTlsPort(5080);
                sipModel.setDefPassword("12345678");
                sipModel.setDtmfMode("rfc4733");
                sipModel.setBindAddress("0.0.0.0");

                sRepo.save(sipModel);
                mapper.writeValue(SIP_SETTINGS, sipModel);
                updateLocalFields(sipModel);
                writeInPjsipconf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                SipModel sipModel = mapper.readValue(SIP_SETTINGS, SipModel.class);
                updateLocalFields(sipModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        
    }

    // --- Update pjsip.conf transport section ---
    private void writeInPjsipconf() {
        File pjsipConfFile = new File("/etc/asterisk/pjsip.conf");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pjsipConfFile, false))) {
            // UDP
            writer.write("[udp]");
            writer.newLine();
            writer.write("type=transport");
            writer.newLine();
            writer.write("protocol=udp");
            writer.newLine();
            writer.write("bind=" + bindAddress + ":" + sipUdpPort);
            writer.newLine();
            writer.newLine();

            // TCP
            writer.write("[tcp]");
            writer.newLine();
            writer.write("type=transport");
            writer.newLine();
            writer.write("protocol=tcp");
            writer.newLine();
            writer.write("bind=" + bindAddress + ":" + sipTcpPort);
            writer.newLine();
            writer.newLine();

            // TLS
            writer.write("[tls]");
            writer.newLine();
            writer.write("type=transport");
            writer.newLine();
            writer.write("protocol=tls");
            writer.newLine();
            writer.write("bind=" + bindAddress + ":" + sipTlsPort);
            writer.newLine();
            writer.write("cert_file=/etc/asterisk/keys/asterisk.pem");
            writer.newLine();
            writer.write("priv_key_file=/etc/asterisk/keys/asterisk.key");
            writer.newLine();
            writer.newLine();

            writer.write("[system]");
            writer.newLine();
            writer.write("type=system");
            writer.newLine();


            writer.flush();
            System.out.println("✔ pjsip.conf transport section written.");
        } catch (IOException e) {
            System.err.println("❌ Error writing pjsip.conf: " + e.getMessage());
        }
    }

    // --- Update Java fields from model ---
    private void updateLocalFields(SipModel sipModel) {
        this.sipUdpPort = sipModel.getSipUdpPort();
        this.sipTcpPort = sipModel.getSipTcpPort();
        this.sipTlsPort = sipModel.getSipTlsPort();
        this.dtmfMode = sipModel.getDtmfMode();
        this.defPassword = sipModel.getDefPassword();
        this.bindAddress = sipModel.getBindAddress();
    }

    // --- Edit from UI/API and save ---
    public boolean editSipSettings(int udpPort, int tcpPort, int tlsPort, String defPass, String dtmf, String bindAddr) {
        try {
            SipModel sModel = sRepo.findById(1L).orElseThrow();
            sModel.setSipUdpPort(udpPort);
            sModel.setSipTcpPort(tcpPort);
            sModel.setSipTlsPort(tlsPort);
            sModel.setDefPassword(defPass);
            sModel.setDtmfMode(dtmf);
            sModel.setBindAddress(bindAddr);

            sRepo.save(sModel);
            mapper.writeValue(SIP_SETTINGS, sModel);
            updateLocalFields(sModel);
            writeInPjsipconf();
            asteriskManager.reloadPJSIP();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Getters ---
    public int getSipUdpPort() {
        return sipUdpPort;
    }

    public int getSipTcpPort() {
        return sipTcpPort;
    }

    public int getSipTlsPort() {
        return sipTlsPort;
    }

    public String getDtmfMode() {
        return dtmfMode;
    }

    public String getDefPassword() {
        return defPassword;
    }

    public String getBindAddress() {
        return bindAddress;
    }
}
