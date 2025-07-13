package com.jaba.p2_t.pbxservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.jaba.p2_t.pbxmodels.*;
import com.jaba.p2_t.pbxrepos.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TrunkService {

    private final PjsipAuthRepositor      authRepo;
    private final PjsipAorRepositor       aorRepo;
    private final PjsipEndpointRepositor  endpointRepo;
    private final TrunkViModelRepository  trunkRepo;
    private final PjsipContactRepository  contactRepo;

    private static final File PJSIP_CONF_PATH =
            new File("/etc/asterisk/pjsip_registrations.conf");

    /** მხოლოდ a‑z, A‑Z, 0‑9, _ - . სიმბოლოებს ვრთავთ. */
    private static final Pattern SAFE_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.-]+$");

    /* ─────────────────────── Public API ─────────────────────── */

    public List<TrunkViModel> getAllTrunk() {
        return trunkRepo.findAll();
    }

    /**
     * ამატებს ან ანახლებს Zadarma‑ს მსგავს ტრანკს.
     * უარს ამბობს, თუ აუცილებელი ველები არ არის ვალიდური.
     */
    @Transactional
    public void addTrunk(
            String login,
            String password,
            String server,
            String fromdomain,
            int    qualify,
            int    channels,
            int    forbiddenInterval,
            int    expiration,
            String transport,
            String name
    ) {

        /* ---------- ჰარდ‑ველიდაცია ---------- */
        if (!isSafe(login) || !isSafe(server) || !isSafe(name)) {
            System.err.println("❌ addTrunk(): login/server/name არასწორია; არ დაემატა");
            return;
        }
        if (!StringUtils.hasText(password)) {
            System.err.println("❌ addTrunk(): პაროლი ცარიელია; არ დაემატა");
            return;
        }

        /* =========== იდენტების შექმნა =========== */
        final String epId   = "trunk-" + login + "-sip";
        final String authId = epId + "-auth";
        final String aorId  = epId + "-aor";
        final String regId  = epId + "-reg";

        /* =========== 1. UI მონაცემი =========== */
        trunkRepo.findById(login).orElseGet(() -> {
            TrunkViModel m = new TrunkViModel();
            m.setId(login);
            m.setTrunkName(name);
            return trunkRepo.save(m);
        });

        /* =========== 2. ps_auths =========== */
        authRepo.findById(authId).orElseGet(() -> {
            PjsipAuth au = new PjsipAuth();
            au.setId(authId);
            au.setAuthType("userpass");
            au.setUsername(login);
            au.setPassword(password);
            return authRepo.save(au);
        });

        /* =========== 3. ps_aors =========== */
        aorRepo.findById(aorId).orElseGet(() -> {
            PjsipAor ao = new PjsipAor();
            ao.setId(aorId);
            ao.setContact("sip:" + login + "@" + server);
            ao.setQualifyFrequency(qualify);
            ao.setMaxContacts(channels);
            ao.setRemoveExisting(true);
            return aorRepo.save(ao);
        });

        /* =========== 4. ps_endpoints =========== */
        endpointRepo.findById(epId).orElseGet(() -> {
            PjsipEndpoint ep = new PjsipEndpoint();
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
            ep.setFromDomain(StringUtils.hasText(fromdomain) ? fromdomain : server);

            ep.setDirectMedia(false);
            ep.setDtmfMode("rfc4733");
            ep.setTrustIdOutbound(true);
            ep.setRewriteContact(true);
            ep.setRtpSymmetric(true);
            ep.setForceRport(true);
            ep.setQualifyFrequency(qualify);

            return endpointRepo.save(ep);
        });

        /* =========== 5. pjsip.conf‑ში რეგისტრაცია =========== */
        writeRegistrationToPjsipConf(
                regId, authId, login, server,
                qualify, forbiddenInterval, expiration,
                transport, epId
        );
    }

    /* ─────────────────────── Helpers ─────────────────────── */

    /** მხოლოდ მაშინ true, როცა ტექსტი არა‑ცარიელია და აკმაყოფილებს SAFE_PATTERN‑ს */
    private static boolean isSafe(String s) {
        return StringUtils.hasText(s) && SAFE_PATTERN.matcher(s).matches();
    }

    /** append‑ს აკეთებს მხოლოდ მაშინ, თუ ასეთი regId უკვე不存在. */
    private void writeRegistrationToPjsipConf(
            String regId,
            String authId,
            String login,
            String server,
            int qualify,
            int forbiddenInterval,
            int expiration,
            String transport,
            String endpointName
    ) {

        try {
            if (!PJSIP_CONF_PATH.exists()) {
                PJSIP_CONF_PATH.getParentFile().mkdirs();
                PJSIP_CONF_PATH.createNewFile();
            }

            Path p = PJSIP_CONF_PATH.toPath();
            String current = Files.readString(p, StandardCharsets.UTF_8);

            if (current.contains("[" + regId + "]")) {
                System.out.println("ℹ️ " + regId + " უკვე არსებობს – გამოტოვებულია");
                return;
            }

            String block =
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

            Files.writeString(p, block, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            System.out.println("✅ " + regId + " დაემატა pjsip.conf‑ში");

        } catch (IOException e) {
            System.err.println("❌ pjsip.conf გადაწერა ვერ მოხერხდა: " + e.getMessage());
        }
    }

    /* ─────────────────────── Trunk removal ─────────────────────── */

    @Transactional
    public Map<String, Object> deleteTrunk(String login) {

        final String epId   = "trunk-" + login + "-sip";
        final String authId = epId + "-auth";
        final String aorId  = epId + "-aor";

        int deleted = 0;

        if (trunkRepo.existsById(login))   { trunkRepo.deleteById(login);   deleted++; }
        if (endpointRepo.existsById(epId)) { endpointRepo.deleteById(epId); deleted++; }
        if (authRepo.existsById(authId))   { authRepo.deleteById(authId);   deleted++; }
        if (aorRepo.existsById(aorId))     { aorRepo.deleteById(aorId);     deleted++; }

        /* contacts */
        List<PjsipContact> contacts = contactRepo.findByEndpoint(epId);
        if (!contacts.isEmpty()) {
            contactRepo.deleteAll(contacts);
            deleted += contacts.size();
        }

        /* pjsip.conf‑დან გამოტანა */
        removeRegistrationFromPjsipConf(login);

        Map<String, Object> res = new HashMap<>();
        if (deleted == 0) {
            res.put("success", false);
            res.put("error", "ვერაფერი მოიძებნა წასაშლელად");
        } else {
            res.put("success", true);
            res.put("deletedCount", deleted);
        }
        return res;
    }

    /** შლის «AUTO‑GENERATED TRUNK (login)» ბლოკს და მის შემდეგ [regId] სექციას */
    private void removeRegistrationFromPjsipConf(String login) {
        if (!PJSIP_CONF_PATH.exists()) return;

        final String regId = "trunk-" + login + "-sip-reg";

        try {
            Path p = PJSIP_CONF_PATH.toPath();
            List<String> in = Files.readAllLines(p, StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();

            boolean inside = false;

            for (String line : in) {
                /* ბლოკის დასაწყისი — ჰედერი ან [regId] */
                if (line.contains("AUTO‑GENERATED TRUNK (" + login + ")") ||
                    line.trim().equals("[" + regId + "]")) {
                    inside = true;
                    continue;        // skip
                }
                /* ბლოკის ბოლო — როცა ახალი [section] იწყება */
                if (inside && line.startsWith("[") && !line.trim().equals("[" + regId + "]")) {
                    inside = false;
                }
                if (!inside) out.add(line);
            }

            Files.write(p, out, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("🧹 წაშლილია რეგისტრაცია: " + login);

        } catch (IOException e) {
            System.err.println("❌ pjsip.conf გაწმენდა ვერ მოხერხდა: " + e.getMessage());
        }
    }
}
