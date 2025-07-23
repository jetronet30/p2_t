package com.jaba.p2_t.pbxservices;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;
import com.jaba.p2_t.pbxmodels.PjsipAor;
import com.jaba.p2_t.pbxmodels.PjsipAuth;
import com.jaba.p2_t.pbxmodels.PjsipContact;
import com.jaba.p2_t.pbxmodels.PjsipEndpoint;
import com.jaba.p2_t.pbxmodels.TrunkViModel;
import com.jaba.p2_t.pbxrepos.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TrunkService {
    private final AsteriskManager asteriskManager;
    private static final File TRUNKS_CONF = new File("/etc/asterisk/custom_trunks.conf");

    private static final Logger log = LoggerFactory.getLogger(TrunkService.class);

    private final PjsipAuthRepositor authRepo;
    private final PjsipAorRepositor aorRepo;
    private final PjsipEndpointRepositor endpointRepo;
    private final TrunkViModelRepository trunkRepo;
    private final PjsipContactRepository contactRepo;

    private static final File PJSIP_CONF_PATH = new File("/etc/asterisk/pjsip_registrations.conf");
    private static final File EXTENSIONS_CONF_PATH = new File("/etc/asterisk/extensions.conf");

    /** მხოლოდ a‑z, A‑Z, 0‑9, _ - . სიმბოლოებს ვრთავთ. */
    private static final Pattern SAFE_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]+$");

    /* ─────────────────────── Public API ─────────────────────── */

    public List<TrunkViModel> getAllTrunk() {
        return trunkRepo.findAll();
    }

    @Transactional
    public void addTrunk(
            String login,
            String password,
            String server,
            String fromdomain,
            int qualify,
            int channels,
            int forbiddenInterval,
            int expiration,
            String transport,
            String name) {

        if (!isSafe(login) || !isSafe(server) || !isSafe(name)) {
            log.warn("addTrunk(): login/server/name არასწორია; არ დაემატა - login='{}', server='{}', name='{}'",
                    login, server, name);
            return;
        }
        if (!StringUtils.hasText(password)) {
            log.warn("addTrunk(): პაროლი ცარიელია; არ დაემატა - login='{}'", login);
            return;
        }

        final String epId = "trunk-" + login + "-sip";
        final String authId = epId + "-auth";
        final String aorId = epId + "-aor";
        final String regId = epId + "-reg";

        // UI მონაცემი - თუ არ არსებობს, შექმენი
        trunkRepo.findById(login).orElseGet(() -> {
            TrunkViModel m = new TrunkViModel();
            m.setId(login);
            m.setTrunkName(name);
            return trunkRepo.save(m);
        });

        // Auth
        authRepo.findById(authId).orElseGet(() -> {
            PjsipAuth au = new PjsipAuth();
            au.setId(authId);
            au.setAuthType("userpass");
            au.setUsername(login);
            au.setPassword(password);
            return authRepo.save(au);
        });

        // Aor
        aorRepo.findById(aorId).orElseGet(() -> {
            PjsipAor ao = new PjsipAor();
            ao.setId(aorId);
            ao.setContact("sip:" + login + "@" + server);
            ao.setQualifyFrequency(qualify);
            ao.setMaxContacts(channels);
            ao.setRemoveExisting(true);
            return aorRepo.save(ao);
        });

        // Endpoint
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

        // რეგისტრაცია pjsip.conf-ში
        writeRegistrationToPjsipConf(
                regId, authId, login, server,
                qualify, forbiddenInterval, expiration,
                transport, epId);
    }

    /* ─────────────────────── Helpers ─────────────────────── */

    private static boolean isSafe(String s) {
        return StringUtils.hasText(s) && SAFE_PATTERN.matcher(s).matches();
    }

    private void writeRegistrationToPjsipConf(
            String regId,
            String authId,
            String login,
            String server,
            int qualify,
            int forbiddenInterval,
            int expiration,
            String transport,
            String endpointName) {

        try {
            if (!PJSIP_CONF_PATH.exists()) {
                PJSIP_CONF_PATH.getParentFile().mkdirs();
                if (!PJSIP_CONF_PATH.createNewFile()) {
                    log.error("writeRegistrationToPjsipConf(): ვერ შექმნა ფაილი {}", PJSIP_CONF_PATH.getAbsolutePath());
                    return;
                }
            }

            Path p = PJSIP_CONF_PATH.toPath();
            String current = Files.readString(p, StandardCharsets.UTF_8);

            if (current.contains("[" + regId + "]")) {
                log.info("{} უკვე არსებობს – გამოტოვებულია", regId);
                return;
            }

            String block = "\n; -------- AUTO‑GENERATED TRUNK (" + login + ") ----------\n" +
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
            log.info("✅ {} დაემატა pjsip.conf‑ში", regId);

        } catch (IOException e) {
            log.error("pjsip.conf გადაწერა ვერ მოხერხდა", e);
        }
    }

    /* ─────────────────────── Trunk Removal ─────────────────────── */

    @Transactional
    public Map<String, Object> deleteTrunk(String login) {
        final String epId = "trunk-" + login + "-sip";
        final String authId = epId + "-auth";
        final String aorId = epId + "-aor";

        int deleted = 0;

        if (trunkRepo.existsById(login)) {
            trunkRepo.deleteById(login);
            deleted++;
        }
        if (endpointRepo.existsById(epId)) {
            endpointRepo.deleteById(epId);
            deleted++;
        }
        if (authRepo.existsById(authId)) {
            authRepo.deleteById(authId);
            deleted++;
        }
        if (aorRepo.existsById(aorId)) {
            aorRepo.deleteById(aorId);
            deleted++;
        }

        List<PjsipContact> contacts = contactRepo.findByEndpoint(epId);
        if (!contacts.isEmpty()) {
            contactRepo.deleteAll(contacts);
            deleted += contacts.size();
        }

        removeRegistrationFromPjsipConf(login);
        removeInboundRoute(login);

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

    private void removeRegistrationFromPjsipConf(String login) {
        if (!PJSIP_CONF_PATH.exists()) {
            log.warn("removeRegistrationFromPjsipConf(): ფაილი {} არ არსებობს", PJSIP_CONF_PATH.getAbsolutePath());
            return;
        }

        final String regId = "trunk-" + login + "-sip-reg";

        try {
            Path p = PJSIP_CONF_PATH.toPath();
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();

            boolean inside = false;

            for (String line : lines) {
                if (line.contains("AUTO‑GENERATED TRUNK (" + login + ")") ||
                        line.trim().equals("[" + regId + "]")) {
                    inside = true;
                    continue;
                }
                if (inside && line.startsWith("[") && !line.trim().equals("[" + regId + "]")) {
                    inside = false;
                }
                if (!inside) {
                    out.add(line);
                }
            }

            Files.write(p, out, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("🧹 წაშლილია რეგისტრაცია: {}", login);

        } catch (IOException e) {
            log.error("pjsip.conf გაწმენდა ვერ მოხერხდა", e);
        }
    }

    private void removeInboundRoute(String trunkId) {
        if (!EXTENSIONS_CONF_PATH.exists()) {
            log.warn("removeInboundRoute(): ფაილი {} არ არსებობს", EXTENSIONS_CONF_PATH.getAbsolutePath());
            return;
        }

        String markerStart = "; >>> AUTOGEN-INBOUND-" + trunkId;
        String markerEnd = "; <<< AUTOGEN-INBOUND-" + trunkId;

        try {
            Path path = EXTENSIONS_CONF_PATH.toPath();
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<String> updated = new ArrayList<>();

            boolean inside = false;

            for (String line : lines) {
                if (line.trim().equals(markerStart)) {
                    inside = true;
                    continue;
                }
                if (line.trim().equals(markerEnd)) {
                    inside = false;
                    continue;
                }
                if (!inside) {
                    updated.add(line);
                }
            }

            Files.write(path, updated, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("🧹 extensions.conf-დან წაშლილია inbound ბლოკი ტრანკისთვის: {}", trunkId);

        } catch (IOException e) {
            log.error("removeInboundRoute(): შეცდომა extensions.conf გაწმენდისას", e);
        }
    }

  
    @Transactional
    public Map<String, Object> setInboundRoute(String trunkId, String destination, String voicemessage) {
        if (!StringUtils.hasText(trunkId)) {
            return Map.of("success", false, "error", "invalid-trunkId");
        }

        if (destination == null || destination.isBlank()) {
            return Map.of("success", false, "error", "invalid-destination");
        }

        if (StringUtils.hasText(voicemessage) && !isSafe(voicemessage)) {
            return Map.of("success", false, "error", "invalid-voicemessage");
        }

        String safeDestination = destination.replaceAll("[^A-Za-z0-9_.-]", "");
        if (!destination.equals(safeDestination)) {
            return Map.of("success", false, "error", "destination-has-illegal-chars");
        }

        if (!EXTENSIONS_CONF_PATH.exists()) {
            log.error("setInboundRoute(): extensions.conf არ არსებობს: {}", EXTENSIONS_CONF_PATH.getAbsolutePath());
            return Map.of("success", false, "error", "extensions-conf-not-found");
        }
        Optional<TrunkViModel> optTrunk = trunkRepo.findById(trunkId);
        if (optTrunk.isPresent()) {
            TrunkViModel trunk = optTrunk.get();
            trunk.setInboundRoute(destination);
            trunk.setVoiceMessage(voicemessage);
            trunkRepo.save(trunk);
            writeInTrunksInboudDialPlan();
            return Map.of("success", true);
        } else {
            log.warn("setInboundRoute(): ტრანკი '{}' ვერ მოიძებნა ბაზაში", trunkId);
            return Map.of("success", false, "error", "trunk-not-found");
        }

    }

    private void writeInTrunksInboudDialPlan() {
        if (TRUNKS_CONF.exists())
            TRUNKS_CONF.delete();
        try {
            TRUNKS_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRUNKS_CONF, true))) {
            writer.write("\n[from-trunk]\n\n");
            for (TrunkViModel tr : trunkRepo.findAll()) {
                if (!tr.getInboundRoute().isEmpty()) {
                    // ამოიღე "-" მარჯვნიდან და დატოვე მისი მარცხნიდან მხოლოდ
                    String inboundRoute = tr.getInboundRoute();
                    int dashIndex = inboundRoute.indexOf("-");
                    if (dashIndex != -1) {
                        inboundRoute = inboundRoute.substring(0, dashIndex); // ამოაქვს მხოლოდ მარცხენა ნაწილი
                    }

                    writer.write("exten => _X.,1,NoOp(Inbound call from trunk: " + tr.getId() + ")\n");
                    writer.write("same => n,Answer()\n");
                    if (!tr.getVoiceMessage().isEmpty())
                        writer.write("same => n,Playback(voicemessages/" + tr.getVoiceMessage() + ")\n");
                    writer.write("same => n,Goto(default," + inboundRoute + ",1)\n");
                    writer.write("same => n,Hangup()\n\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        asteriskManager.reloadDialplan();
        
    }

}
