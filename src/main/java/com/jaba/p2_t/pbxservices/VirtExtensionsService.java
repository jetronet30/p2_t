package com.jaba.p2_t.pbxservices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaba.p2_t.pbxmodels.ExtenViModel;
import com.jaba.p2_t.pbxmodels.PjsipAor;
import com.jaba.p2_t.pbxmodels.PjsipAuth;
import com.jaba.p2_t.pbxmodels.PjsipContact;
import com.jaba.p2_t.pbxmodels.PjsipEndpoint;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import com.jaba.p2_t.pbxrepos.PjsipAorRepositor;
import com.jaba.p2_t.pbxrepos.PjsipAuthRepositor;
import com.jaba.p2_t.pbxrepos.PjsipContactRepository;
import com.jaba.p2_t.pbxrepos.PjsipEndpointRepositor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VirtExtensionsService {

    private final SipSettings sipSettings;
    private final PjsipEndpointRepositor pjsipEndpointRepositor;
    private final PjsipAuthRepositor pjsipAuthRepositor;
    private final PjsipAorRepositor pjsipAorRepositor;
    private final ExtenVirtualRepo extenVirtualRepo;
    private final PjsipContactRepository pjsipContactRepository;

    public List<ExtenViModel> getVirtExts() {
        return extenVirtualRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public void createVirtExtInRange(String start, String stop) {
        int first, last;
        try {
            first = Integer.parseInt(start.trim());
            last = Integer.parseInt(stop.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("საწყისი ან საბოლოო ID არ არის რიცხვი", ex);
        }
        if (first > last)
            throw new IllegalArgumentException("საწყისი ID მეტია საბოლოოზე");
        if (last - first > 10_000)
            throw new IllegalArgumentException("დიაპაზონი ძალიან დიდია (>10 000)");

        Set<String> existingIds = new HashSet<>(pjsipEndpointRepositor.findAllIds());

        int batchSize = 50;
        List<ExtenViModel> viModels = new ArrayList<>();
        List<PjsipEndpoint> endpoints = new ArrayList<>();
        List<PjsipAuth> auths = new ArrayList<>();
        List<PjsipAor> aors = new ArrayList<>();

        for (int i = first; i <= last; i++) {
            String id = String.valueOf(i);
            if (existingIds.contains(id))
                continue;

            ExtenViModel vi = new ExtenViModel();
            vi.setId(id);
            vi.setDisplayName(id);
            vi.setActive(false);
            vi.setOutPermit(3);
            vi.setVirContext("default");
            vi.setVirPass(sipSettings.getDefPassword());
            viModels.add(vi);

            PjsipEndpoint ep = new PjsipEndpoint();
            ep.setId(id);
            ep.setAorsId(id);
            ep.setAuthId(id);
            ep.setType("endpoint");
            ep.setTransport("udp");
            ep.setContext("default");
            ep.setDisallow("all");
            ep.setAllow("ulaw,alaw");
            ep.setDtmfMode(sipSettings.getDtmfMode());
            ep.setDirectMedia(false);
            ep.setCallerId(id + "<" + id + ">");
            endpoints.add(ep);

            PjsipAuth au = new PjsipAuth();
            au.setId(id);
            au.setAuthType("userpass");
            au.setUsername(id);
            au.setPassword(sipSettings.getDefPassword());
            auths.add(au);

            PjsipAor ao = new PjsipAor();
            ao.setId(id);
            ao.setMaxContacts(1);
            ao.setQualifyFrequency(30);
            aors.add(ao);

            if (viModels.size() >= batchSize) {
                extenVirtualRepo.saveAll(viModels);
                pjsipEndpointRepositor.saveAll(endpoints);
                pjsipAuthRepositor.saveAll(auths);
                pjsipAorRepositor.saveAll(aors);

                viModels.clear();
                endpoints.clear();
                auths.clear();
                aors.clear();
            }
        }

        if (!viModels.isEmpty()) {
            extenVirtualRepo.saveAll(viModels);
            pjsipEndpointRepositor.saveAll(endpoints);
            pjsipAuthRepositor.saveAll(auths);
            pjsipAorRepositor.saveAll(aors);
        }
    }

    @Transactional
    public void createVirtExt(String extensionId) {

        if (!pjsipEndpointRepositor.existsById(extensionId) &&
                !pjsipAuthRepositor.existsById(extensionId) &&
                !pjsipAorRepositor.existsById(extensionId)) {

            ExtenViModel viModel = new ExtenViModel();
            viModel.setId(extensionId);
            viModel.setDisplayName(extensionId);
            viModel.setActive(false);
            viModel.setOutPermit(3);
            viModel.setVirContext("default");
            viModel.setVirPass(sipSettings.getDefPassword());

            PjsipEndpoint endpoint = new PjsipEndpoint();
            endpoint.setId(extensionId);
            endpoint.setAorsId(extensionId);
            endpoint.setAuthId(extensionId);
            endpoint.setType("endpoint");
            endpoint.setTransport("udp");
            endpoint.setContext("default");
            endpoint.setDisallow("all");
            endpoint.setAllow("ulaw,alaw");
            endpoint.setDtmfMode(sipSettings.getDtmfMode());
            endpoint.setDirectMedia(false);
            endpoint.setCallerId(extensionId + "<" + extensionId + ">");

            PjsipAuth auth = new PjsipAuth();
            auth.setId(extensionId);
            auth.setAuthType("userpass");
            auth.setUsername(extensionId);
            auth.setPassword(sipSettings.getDefPassword());

            PjsipAor aor = new PjsipAor();
            aor.setId(extensionId);
            aor.setMaxContacts(1);
            aor.setQualifyFrequency(30);

            extenVirtualRepo.save(viModel);
            pjsipEndpointRepositor.save(endpoint);
            pjsipAuthRepositor.save(auth);
            pjsipAorRepositor.save(aor);
        }
    }

    @Transactional
    public Map<String, Object> updateVirtExt(String extensionId, String displayName, String virContext, String virPass,
            int outPermit, String rezerve_1, String rezerve_2) {
        Map<String, Object> result = new HashMap<>();

        Optional<ExtenViModel> viModelOpt = extenVirtualRepo.findById(extensionId);
        Optional<PjsipEndpoint> endpointOpt = pjsipEndpointRepositor.findById(extensionId);
        Optional<PjsipAuth> authOpt = pjsipAuthRepositor.findById(extensionId);
        Optional<PjsipAor> aorOpt = pjsipAorRepositor.findById(extensionId);

        if (viModelOpt.isEmpty() || endpointOpt.isEmpty() || authOpt.isEmpty() || aorOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "ვერ მოიძებნა ყველა აუცილებელი ჩანაწერი");
            return result;
        }

        ExtenViModel viModel = viModelOpt.get();
        viModel.setDisplayName(displayName);
        viModel.setVirContext(virContext);
        viModel.setVirPass(virPass);
        viModel.setOutPermit(outPermit);
        viModel.setRezerve1(rezerve_1);
        viModel.setRezerve2(rezerve_2);

        PjsipEndpoint endpoint = endpointOpt.get();
        endpoint.setAorsId(extensionId);
        endpoint.setAuthId(extensionId);
        endpoint.setType("endpoint");
        endpoint.setTransport("udp");
        endpoint.setContext(virContext);
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDtmfMode(sipSettings.getDtmfMode());
        endpoint.setDirectMedia(false);
        endpoint.setCallerId(displayName + "<" + displayName + ">");

        PjsipAuth auth = authOpt.get();
        auth.setAuthType("userpass");
        auth.setUsername(extensionId);
        auth.setPassword(virPass);

        PjsipAor aor = aorOpt.get();
        aor.setMaxContacts(1);

        extenVirtualRepo.save(viModel);
        pjsipEndpointRepositor.save(endpoint);
        pjsipAuthRepositor.save(auth);

        try {
            Path dialplanFile = Paths.get("/etc/asterisk/extensions.conf");
            List<String> lines = Files.readAllLines(dialplanFile);
            String markerStart = ";-- start auto-forward " + extensionId;
            String markerEnd = ";-- end auto-forward " + extensionId;

            // ძველი ჩანაწერის წაშლა
            int startIdx = -1, endIdx = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals(markerStart))
                    startIdx = i;
                if (lines.get(i).trim().equals(markerEnd)) {
                    endIdx = i;
                    break;
                }
            }
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                lines.subList(startIdx, endIdx + 1).clear();
            }

            boolean hasReserve1 = rezerve_1 != null && !rezerve_1.isBlank();
            boolean hasReserve2 = rezerve_2 != null && !rezerve_2.isBlank();

            // თუ პირველი რეზერვი არ არის, მეორე გადავინაცვლოთ პირველზე
            if (!hasReserve1 && hasReserve2) {
                rezerve_1 = rezerve_2;
                rezerve_2 = null;
                hasReserve1 = true;
                hasReserve2 = false;
            }

            // თუ არცერთი რეზერვი არ არის, ჩანაწერს ვშლი და ვაბრუნებთ წარმატებას
            if (!hasReserve1) {
                Files.write(dialplanFile, lines);
                result.put("extensions_updated", true);
                result.put("message", "არ არის რეზერვები, ჩანაწერი წაიშალა");
                result.put("success", true);
                return result;
            }

            List<String> newBlock = new ArrayList<>();
            newBlock.add(markerStart);

            // კონტექსტი არ უნდა გამეორდეს, თუ default არის
            if (!"default".equalsIgnoreCase(virContext)) {
                newBlock.add("[" + virContext + "]");
            }

            newBlock.add("exten => " + extensionId + ",1,NoOp(Forward check for " + extensionId + ")");
            newBlock.add(" same => n,Dial(PJSIP/" + extensionId + ",30)");

            // რეზერვული გადამისამართების ლოგიკა
            if (hasReserve1) {
                newBlock.add(
                        " same => n,GotoIf($[\"${DIALSTATUS}\" = \"BUSY\" | \"${DIALSTATUS}\" = \"NOANSWER\"]?firstres)");
                newBlock.add(" same => n(firstres),NoOp(Forwarding to first reserve " + rezerve_1 + ")");
                newBlock.add(" same => n,Dial(PJSIP/" + rezerve_1 + ",30)");

                if (hasReserve2) {
                    newBlock.add(
                            " same => n,GotoIf($[\"${DIALSTATUS}\" = \"BUSY\" | \"${DIALSTATUS}\" = \"NOANSWER\"]?secondres)");
                } else {
                    newBlock.add(" same => n,Hangup()");
                }
            }

            if (hasReserve2) {
                newBlock.add(" same => n(secondres),NoOp(Forwarding to second reserve " + rezerve_2 + ")");
                newBlock.add(" same => n,Dial(PJSIP/" + rezerve_2 + ",30)");
                newBlock.add(" same => n,Hangup()");
            } else if (!hasReserve1) {
                // თუ არც პირველი და არც მეორე არ არის, აქაც უნდა დაიხუროს ზარი
                newBlock.add(" same => n,Hangup()");
            } else if (!hasReserve2) {
                // თუ არის პირველი რეზერვი და არაა მეორე, მხოლოდ ერთი Hangup
                // ეს უკვე მიყვანილია ზემოთ.
                // აქ არაფერი არ გვინდა დამატება.
            }

            newBlock.add(markerEnd);

            lines.add("");
            lines.addAll(newBlock);

            Files.write(dialplanFile, lines);
            result.put("extensions_updated", true);

        } catch (IOException e) {
            result.put("extensions_updated", false);
            result.put("error_extensions", e.getMessage());
        }

        result.put("success", true);
        return result;
    }

    @Transactional
    public Map<String, Object> deleteVirtExt(String extensionId) {
        Map<String, Object> result = new HashMap<>();
        int deletedCount = 0;

        if (extenVirtualRepo.existsById(extensionId)) {
            extenVirtualRepo.deleteById(extensionId);
            deletedCount++;
        }
        if (pjsipEndpointRepositor.existsById(extensionId)) {
            pjsipEndpointRepositor.deleteById(extensionId);
            deletedCount++;
        }
        if (pjsipAuthRepositor.existsById(extensionId)) {
            pjsipAuthRepositor.deleteById(extensionId);
            deletedCount++;
        }
        if (pjsipAorRepositor.existsById(extensionId)) {
            pjsipAorRepositor.deleteById(extensionId);
            deletedCount++;
        }

        // წავშალოთ ყველა კონტაქტი იმ ენდპოინტისთვის, რომელიც იდ-ს შეესაბამება
        List<PjsipContact> contacts = pjsipContactRepository.findByEndpoint(extensionId);
        if (!contacts.isEmpty()) {
            pjsipContactRepository.deleteAll(contacts);
            deletedCount += contacts.size();
        }

        // დამატებითი ლოგიკა: წაშალე extensions.conf-დან ავტომატური ფორვარდის ჩანაწერი
        try {
            Path dialplanFile = Paths.get("/etc/asterisk/extensions.conf"); // საჭიროებისამებრ შეცვალე
            List<String> lines = Files.readAllLines(dialplanFile);
            String markerStart = ";-- start auto-forward " + extensionId;
            String markerEnd = ";-- end auto-forward " + extensionId;

            int startIdx = -1, endIdx = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals(markerStart))
                    startIdx = i;
                if (lines.get(i).trim().equals(markerEnd)) {
                    endIdx = i;
                    break;
                }
            }

            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                lines.subList(startIdx, endIdx + 1).clear();
                Files.write(dialplanFile, lines);
                result.put("extensions_updated", true);
            } else {
                result.put("extensions_updated", false);
                result.put("extensions_message", "Extensions dialplan block not found for deletion");
            }
        } catch (IOException e) {
            result.put("extensions_updated", false);
            result.put("error_extensions", e.getMessage());
        }

        if (deletedCount == 0) {
            result.put("success", false);
            result.put("message", "ვერც ერთი ჩანაწერი ვერ მოიძებნა");
            return result;
        }

        result.put("success", true);
        result.put("deletedCount", deletedCount);
        return result;
    }

    public void updateUserIp() {
        extenVirtualRepo.findAll().forEach(ext -> {
            String pattern = ext.getId() + "^"; // მაგ: 1105^
            pjsipContactRepository.findAll().stream()
                    .filter(contact -> contact.getId().startsWith(pattern))
                    .findFirst()
                    .ifPresent(contact -> {
                        ext.setVirUsIp(contact.getViaAddr());
                        ext.setModelName(getFirstPart(contact.getUserAgent()));
                        extenVirtualRepo.save(ext);
                    });
        });
    }

    private String getFirstPart(String input) {
        if (input == null || input.isEmpty())
            return "";
        String[] parts = input.split("[ ./,]", 2);
        return parts[0];
    }

}
