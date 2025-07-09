package com.jaba.p2_t.pbxservices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.jaba.p2_t.pbxmodels.PjsipAor;
import com.jaba.p2_t.pbxmodels.PjsipAuth;
import com.jaba.p2_t.pbxmodels.PjsipEndpoint;
import com.jaba.p2_t.pbxrepos.PjsipAorRepositor;
import com.jaba.p2_t.pbxrepos.PjsipAuthRepositor;
import com.jaba.p2_t.pbxrepos.PjsipEndpointRepositor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtensionService {
    private final TrunkService tService;
    
    private final SipSettingsInit settingsInit;
    private final PjsipEndpointRepositor pjsipEndpointRepositor;
    private final PjsipAuthRepositor pAuthRepositor;
    private final PjsipAorRepositor pAorRepositor;

    @Transactional
    public void addExtensionsRange(String start, String end) {
        try {
            int from = Integer.parseInt(start);
            int to = Integer.parseInt(end);

            if (from > to) {
                System.out.println("Invalid range: start > end");
                return;
            }

            Set<String> existingIds = new HashSet<>(pjsipEndpointRepositor.findAllIds());

            List<PjsipEndpoint> endpoints = new ArrayList<>();
            List<PjsipAuth> auths = new ArrayList<>();
            List<PjsipAor> aors = new ArrayList<>();

            for (int i = from; i <= to; i++) {
                String userId = String.valueOf(i);

                if (!existingIds.contains(userId)) {
                    PjsipEndpoint endpoint = new PjsipEndpoint();
                    endpoint.setId(userId);
                    endpoint.setAorsId(userId);
                    endpoint.setAuthId(userId);
                    endpoint.setType("endpoint");
                    endpoint.setTransport("udp");
                    endpoint.setContext("default");
                    endpoint.setDisallow("all");
                    endpoint.setAllow("ulaw,alaw");
                    endpoint.setDtmfMode(settingsInit.getDtmfMode());
                    endpoint.setDirectMedia(false);
                    endpoint.setCallerId(userId + "<" + userId + ">");
                    endpoint.setShower(settingsInit.getDefPassword());

                    PjsipAuth auth = new PjsipAuth();
                    auth.setId(userId);
                    auth.setAuthType("userpass");
                    auth.setUsername(userId);
                    auth.setPassword(settingsInit.getDefPassword());

                    PjsipAor aor = new PjsipAor();
                    aor.setId(userId);
                    aor.setMaxContacts(100);

                    endpoints.add(endpoint);
                    auths.add(auth);
                    aors.add(aor);
                }
            }

            pjsipEndpointRepositor.saveAll(endpoints);
            pAuthRepositor.saveAll(auths);
            pAorRepositor.saveAll(aors);
            

            System.out.println("Extensions added: " + start + " to " + end);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input: not a number");
        }
    }

    public void addExtension(String userId) {
        tService.addZadarmaTrunkToDb();
        if (!pjsipEndpointRepositor.existsById(userId) && !pAuthRepositor.existsById(userId)
                && !pAorRepositor.existsById(userId)) {
            PjsipEndpoint endpoint = new PjsipEndpoint();
            PjsipAuth auth = new PjsipAuth();
            PjsipAor aor = new PjsipAor();
            endpoint.setId(userId);
            endpoint.setAorsId(userId);
            endpoint.setAuthId(userId);
            endpoint.setType("endpoint");
            endpoint.setTransport("udp");
            endpoint.setContext("default");
            endpoint.setDisallow("all");
            endpoint.setAllow("ulaw,alaw");
            endpoint.setDtmfMode(settingsInit.getDtmfMode());
            endpoint.setDirectMedia(false);
            endpoint.setCallerId(userId + "<" + userId + ">");
            endpoint.setShower(settingsInit.getDefPassword());
            auth.setId(userId);
            auth.setAuthType("userpass");
            auth.setUsername(userId);
            auth.setPassword(settingsInit.getDefPassword());
            aor.setId(userId);
            aor.setMaxContacts(100);
            pjsipEndpointRepositor.save(endpoint);
            pAuthRepositor.save(auth);
            pAorRepositor.save(aor);
        }
    }

    public void editExtension(String id, String callerId, String context, String password) {
        PjsipEndpoint endpoint = pjsipEndpointRepositor.findById(id).orElse(null);
        PjsipAuth auth = pAuthRepositor.findById(id).orElse(null);
        PjsipAor aor = pAorRepositor.findById(id).orElse(null);
        if (endpoint != null && auth != null && aor != null) {
            endpoint.setCallerId(callerId);
            endpoint.setContext(context);
            endpoint.setShower(password);
            auth.setPassword(password);
            pjsipEndpointRepositor.save(endpoint);
            pAuthRepositor.save(auth);
            pAorRepositor.save(aor);
        }
    }

    public boolean deleteExtension(String id) {
        pjsipEndpointRepositor.deleteById(id);
        pAuthRepositor.deleteById(id);
        pAorRepositor.deleteById(id);
        return true;
    }

    public List<PjsipEndpoint> getAllExtensions() {
        return pjsipEndpointRepositor.findAll();
    }

    public List<PjsipEndpoint> getAllExtensionsSorteId() {
        return pjsipEndpointRepositor.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public boolean generatePjsipConf(String filePath) {
        List<PjsipEndpoint> endpoints = pjsipEndpointRepositor.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("[transport-udp]");
            writer.newLine();
            writer.write("type=transport");
            writer.newLine();
            writer.write("protocol=udp");
            writer.newLine();
            writer.write("bind=0.0.0.0");
            writer.newLine();
            writer.newLine();

            for (PjsipEndpoint endpoint : endpoints) {
                String id = endpoint.getId();

                writer.write("[" + id + "]");
                writer.newLine();
                writer.write("type=endpoint");
                writer.newLine();
                writer.write("context=" + endpoint.getContext());
                writer.newLine();
                writer.write("disallow=" + endpoint.getDisallow());
                writer.newLine();
                writer.write("allow=" + endpoint.getAllow());
                writer.newLine();
                writer.write("auth=" + endpoint.getAuthId());
                writer.newLine();
                writer.write("aors=" + endpoint.getAorsId());
                writer.newLine();
                writer.write("dtmf_mode=" + endpoint.getDtmfMode());
                writer.newLine();
                writer.write("direct_media=no");
                writer.newLine();
                writer.write("callerid=" + endpoint.getCallerId());
                writer.newLine();
                writer.write("transport=" + endpoint.getTransport());
                writer.newLine();
                writer.newLine();

                PjsipAuth auth = pAuthRepositor.findById(id).orElse(null);
                if (auth != null) {
                    writer.write("[" + id + "]");
                    writer.newLine();
                    writer.write("type=auth");
                    writer.newLine();
                    writer.write("auth_type=" + auth.getAuthType());
                    writer.newLine();
                    writer.write("username=" + auth.getUsername());
                    writer.newLine();
                    writer.write("password=" + auth.getPassword());
                    writer.newLine();
                    writer.newLine();
                }

                PjsipAor aor = pAorRepositor.findById(id).orElse(null);
                if (aor != null) {
                    writer.write("[" + id + "]");
                    writer.newLine();
                    writer.write("type=aor");
                    writer.newLine();
                    writer.write("max_contacts=" + aor.getMaxContacts());
                    writer.newLine();
                    writer.newLine();
                }
            }

            writer.flush();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateEndpointsConnIpFromCli() {
        try {
            ProcessBuilder pb = new ProcessBuilder("asterisk", "-rx", "pjsip show contacts");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern pattern = Pattern.compile("Contact:\\s+(\\S+)/sip:[^@]+@([0-9.]+):\\d+");

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String endpointId = matcher.group(1);
                        String ip = matcher.group(2);

                        pjsipEndpointRepositor.findById(endpointId).ifPresent(endpoint -> {
                            endpoint.setConnIp(ip);
                            pjsipEndpointRepositor.save(endpoint);
                        });
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("asterisk command exited with code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
