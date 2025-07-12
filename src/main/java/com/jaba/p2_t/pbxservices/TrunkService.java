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
            auth.setPassword("t0dTyUElK3");
            authRepo.save(auth);
        }

        // --- AOR ---
        if (!aorRepo.existsById("trunk-123403-sip-aor")) {
            PjsipAor aor = new PjsipAor();
            aor.setId("trunk-123403-sip-aor");
            aor.setContact("sip:123403@sip.zadarma.com");
            aor.setQualifyFrequency(30);
            aor.setMaxContacts(3);
            aor.setRemoveExisting(true);
            aorRepo.save(aor);
        }

        // --- Endpoint ---
        if (!endpointRepo.existsById("trunk-123403-sip")) {
            PjsipEndpoint ep = new PjsipEndpoint();
            ep.setId("trunk-123403-sip");
            ep.setType("endpoint");

            /* ───── სატრანსპორტო და აუდიოს პარამეტრები ───── */
            ep.setTransport("udp");
            ep.setDisallow("all");
            ep.setAllow("ulaw,alaw");

            /* ───── Dial‑plan / კონტრექსტი ───── */
            ep.setContext("from-trunk"); // სადაც შემოვლენ ზარები

            /* ───── ავტორიზაციის, AOR და დაკავშირებული ველები ───── */
            ep.setAuthId("trunk-123403-sip-auth"); // <== ps_auths.id
            ep.setAorsId("trunk-123403-sip-aor"); // <== ps_aors.id
            ep.setOutboundAuth("trunk-123403-sip-auth"); // აუცილებელია რეგისტრაციისას

            /* ───── From header ველები ───── */
            ep.setFromUser("123403"); // login
            ep.setFromDomain("sip.zadarma.com");

            /* ───── Caller‑ID და მედიის ვარიანტები ───── */
            ep.setCallerId("123403 <123403>");
            ep.setDirectMedia(false);
            ep.setDtmfMode("rfc4733");

            /* ───── NAT/Qualify ოპციები ───── */
            ep.setTrustIdOutbound(true);
            ep.setRewriteContact(true);
            ep.setRtpSymmetric(true);
            ep.setForceRport(true);
            ep.setQualifyFrequency(60);

            /* ───── შენახვა ───── */
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
