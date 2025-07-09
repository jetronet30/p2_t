package com.jaba.p2_t.pbxservices;

import org.springframework.stereotype.Service;
import com.jaba.p2_t.pbxrepos.PjsipAorRepositor;
import com.jaba.p2_t.pbxrepos.PjsipAuthRepositor;
import com.jaba.p2_t.pbxrepos.PjsipEndpointRepositor;
import com.jaba.p2_t.pbxmodels.PjsipAor;
import com.jaba.p2_t.pbxmodels.PjsipAuth;
import com.jaba.p2_t.pbxmodels.PjsipEndpoint;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class TrunkService {

    private final PjsipAuthRepositor authRepo;
    private final PjsipAorRepositor aorRepo;
    private final PjsipEndpointRepositor endpointRepo;

    private static final String PJSIP_CONF_PATH = "/etc/asterisk/pjsip.conf";

    public void addZadarmaTrunkToDb() {
        // --- Auth ---
        if (!authRepo.existsById("trunk-123403-sip-auth")) {
            PjsipAuth auth = new PjsipAuth();
            auth.setId("trunk-123403-sip-auth");
            auth.setAuthType("userpass");
            auth.setUsername("123403");
            auth.setPassword("QtmLfFE7x6");
            authRepo.save(auth);
        }

        // --- AOR ---
        if (!aorRepo.existsById("trunk-123403-sip-aor")) {
            PjsipAor aor = new PjsipAor();
            aor.setId("trunk-123403-sip-aor");
            aor.setContact("sip:123403@sip.zadarma.com");
            aor.setMaxContacts(1);
            aor.setRemoveExisting(true);
            aorRepo.save(aor);
        }

        // --- Endpoint ---
        if (!endpointRepo.existsById("trunk-123403-sip")) {
            PjsipEndpoint ep = new PjsipEndpoint();
            ep.setId("trunk-123403-sip");
            ep.setType("endpoint");
            ep.setTransport("udp");
            ep.setContext("from-trunk");
            ep.setDisallow("all");
            ep.setAllow("ulaw,alaw");
            ep.setAuthId("trunk-123403-sip-auth");
            ep.setAorsId("trunk-123403-sip-aor");
            ep.setCallerId("123403 <123403>");
            ep.setDirectMedia(false);
            ep.setDtmfMode("rfc4733");
            ep.setTrustIdOutbound(true);
            ep.setFromDomain("sip.zadarma.com");
            ep.setRewriteContact(true);
            ep.setQualifyFrequency(60);
            ep.setRtpSymmetric(true);
            ep.setForceRport(true);
            endpointRepo.save(ep);
        }

        // --- რეგისტრაცია პირდაპირ pjsip.conf-ში ---
        writeRegistrationToPjsipConf();
    }

    private void writeRegistrationToPjsipConf() {
        String block = """
                
                ; Zadarma Trunk Registration (written by TrunkService)
                [trunk-123403-sip-reg]
                type=registration
                outbound_auth=trunk-123403-sip-auth
                server_uri=sip:sip.zadarma.com
                client_uri=sip:123403@sip.zadarma.com
                retry_interval=60
                forbidden_retry_interval=600
                expiration=3600
                transport=udp
                endpoint=trunk-123403-sip
                line=yes
                support_path=yes

                """;

        try {
            Path path = Paths.get(PJSIP_CONF_PATH);
            String existing = Files.readString(path);

            if (!existing.contains("[trunk-123403-sip-reg]")) {
                Files.writeString(path, block, StandardOpenOption.APPEND);
                System.out.println("✅ trunk-123403-sip-reg added to pjsip.conf");
            } else {
                System.out.println("ℹ️ trunk-123403-sip-reg already exists in pjsip.conf");
            }
        } catch (IOException e) {
            System.err.println("❌ ვერ ჩაიწერა pjsip.conf-ში: " + e.getMessage());
        }
    }
}
