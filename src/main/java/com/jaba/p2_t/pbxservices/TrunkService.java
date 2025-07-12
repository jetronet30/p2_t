package com.jaba.p2_t.pbxservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaba.p2_t.pbxmodels.*;
import com.jaba.p2_t.pbxrepos.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrunkService {

    private final PjsipAuthRepositor      authRepo;
    private final PjsipAorRepositor       aorRepo;
    private final PjsipEndpointRepositor  endpointRepo;
    private final TrunkViModelRepository  trunkRepo;
    private final PjsipContactRepository  contactRepo;

    /* სად მდებარეობს სტატიკური pjsip.conf (realtime‑ს გარეშე) */
    private static final String PJSIP_CONF_PATH = "/etc/asterisk/pjsip.conf";

    /* ───────────────────────── Public API ───────────────────────── */

    public List<TrunkViModel> getAllTrunk() {
        return trunkRepo.findAll();
    }

    /** Adds / rewrites a new Zadarma‑style trunk (auth+aor+endpoint+registration). */
    @Transactional
    public void addTrunk(
            String login,                    // 123403
            String password,
            String server,                   // sip.zadarma.com
            String fromdomain,               // optional
            int    qualify,
            int    channels,
            int    forbiddenInterval,
            int    expiration,
            String transport                // udp / tcp …
    ) {

        /* ==== შევთანხმდეთ იდენტიფიკატორებზე ==== */
        final String epId   = "trunk-" + login + "-sip";         // endpoint
        final String authId = epId + "-auth";                    // auth
        final String aorId  = epId + "-aor";                     // aor
        final String regId  = epId + "-reg";                     // registration (pjsip.conf)

      

        /* ==== 2) ps_auths ==== */
        if (!authRepo.existsById(authId)) {
            var au = new PjsipAuth();
            au.setId(authId);
            au.setAuthType("userpass");
            au.setUsername(login);
            au.setPassword(password);
            authRepo.save(au);
        }

        /* ==== 3) ps_aors ==== */
        if (!aorRepo.existsById(aorId)) {
            var ao = new PjsipAor();
            ao.setId(aorId);
            ao.setContact("sip:" + login + "@" + server);
            ao.setQualifyFrequency(qualify);
            ao.setMaxContacts(channels);
            ao.setRemoveExisting(true);
            aorRepo.save(ao);
        }

        /* ==== 4) ps_endpoints ==== */
        if (!endpointRepo.existsById(epId)) {
            var ep = new PjsipEndpoint();
            ep.setId(epId);
            ep.setType("endpoint");

            ep.setTransport(transport);
            ep.setContext("from-trunk");
            ep.setDisallow("all");
            ep.setAllow("ulaw,alaw");

            ep.setAuthId(authId);
            ep.setAorsId(aorId);
            ep.setOutboundAuth(authId);

            ep.setCallerId(login + " <" + login + ">");
            ep.setFromUser(login);
            ep.setFromDomain((fromdomain != null && !fromdomain.isBlank()) ? fromdomain : server);

            ep.setDirectMedia(false);
            ep.setDtmfMode("rfc4733");

            ep.setTrustIdOutbound(true);
            ep.setRewriteContact(true);
            ep.setRtpSymmetric(true);
            ep.setForceRport(true);
            ep.setQualifyFrequency(qualify);

            endpointRepo.save(ep);
        }

        /* ==== 5) static registration მონაკვეთი pjsip.conf‑ში ==== */
        writeRegistrationToPjsipConf(regId, authId, login, server,
                                     qualify, forbiddenInterval, expiration, transport, epId);
    }

    /* ───────────────────────── helpers ───────────────────────── */

    /** Append a [registration] section iff it doesn't exist yet. */
    private void writeRegistrationToPjsipConf(String regId, String authId,
                                              String login, String server,
                                              int qualify, int forbiddenInterval,
                                              int expiration, String transport,
                                              String endpointName) {

        final String block =
            "\n; -------- AUTO‑GENERATED TRUNK (" + login + ") ----------\n" +
            "[" + regId + "]\n" +
            "type=registration\n" +
            "outbound_auth=" + authId + "\n" +
            "server_uri=sip:" + server + "\n" +
            "client_uri=sip:" + login + "@" + server + "\n" +
            "retry_interval=" + qualify + "\n" +
            "forbidden_retry_interval=" + forbiddenInterval + "\n" +
            "expiration=" + expiration + "\n" +
            "transport=" + transport + "\n" +
            "endpoint=" + endpointName + "\n" +
            "line=yes\n" +
            "support_path=yes\n";

        try {
            Path p = Paths.get(PJSIP_CONF_PATH);
            String existing = Files.readString(p);

            if (!existing.contains("[" + regId + "]")) {
                Files.writeString(p, block, StandardOpenOption.APPEND);
                System.out.println("✅ " + regId + " appended to pjsip.conf");
            } else {
                System.out.println("ℹ️ " + regId + " already present – skipped");
            }
        } catch (IOException ex) {
            System.err.println("❌ pjsip.conf write failed: " + ex.getMessage());
        }
    }

    /* ======= Trunk removal ======= */
    @Transactional
    public Map<String,Object> deleteTrunk(String login) {

        String epId   = "trunk-" + login + "-sip";
        String authId = epId + "-auth";
        String aorId  = epId + "-aor";

        int deleted = 0;

        if (trunkRepo.existsById(login))   { trunkRepo.deleteById(login); deleted++; }
        if (endpointRepo.existsById(epId)) { endpointRepo.deleteById(epId); deleted++; }
        if (authRepo.existsById(authId))   { authRepo.deleteById(authId); deleted++; }
        if (aorRepo.existsById(aorId))     { aorRepo.deleteById(aorId); deleted++; }

        /* remove related contacts */
        List<PjsipContact> cts = contactRepo.findByEndpoint(epId);
        if (!cts.isEmpty()) { contactRepo.deleteAll(cts); deleted += cts.size(); }

        Map<String,Object> res = new HashMap<>();
        if (deleted == 0) {
            res.put("success", false);
            res.put("error", "ვერაფერი მოიძებნა წასაშლელად");
        } else {
            res.put("success", true);
            res.put("deletedCount", deleted);
        }
        return res;
    }
}
