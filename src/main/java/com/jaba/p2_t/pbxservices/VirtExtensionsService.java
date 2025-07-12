package com.jaba.p2_t.pbxservices;

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
            int outPermit) {
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

    private  String getFirstPart(String input) {
        if (input == null || input.isEmpty())
            return "";
        String[] parts = input.split("[ ./,]", 2); 
        return parts[0];
    }

}
