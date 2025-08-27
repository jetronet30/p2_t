package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;
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
import com.jaba.p2_t.pms.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VirtExtensionsService {

    private final AsteriskManager asteriskManager;
    private static final File FORWARDING_CONF = new File("/etc/asterisk/autoforwarding.conf");
    private static final File PERMIT_CONF = new File("/etc/asterisk/outpermit.conf");
    private static final File PERMIT_RECORDING = new File("/etc/asterisk/permitrecording.conf");

    private final SipSettings sipSettings;
    private final PjsipEndpointRepositor pjsipEndpointRepositor;
    private final PjsipAuthRepositor pjsipAuthRepositor;
    private final PjsipAorRepositor pjsipAorRepositor;
    private final ExtenVirtualRepo extenVirtualRepo;
    private final PjsipContactRepository pjsipContactRepository;

    private final RoomService roomService;

    public List<ExtenViModel> getVirtExts() {
        return extenVirtualRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public void createVirtExtInRange(String start, String stop, boolean isRoom) {
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
            vi.setRecordPermit(false);
            vi.setRoom(isRoom);
            vi.setVoiceMessage(null);
            viModels.add(vi);
            roomService.addRoom(id, isRoom);

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
    public void createVirtExt(String extensionId, boolean isRoom) {

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
            viModel.setRoom(false);
            viModel.setVoiceMessage(null);
            viModel.setRecordPermit(false);
            viModel.setRoom(isRoom);

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
            roomService.addRoom(extensionId, isRoom);
            writeAutoForvarding();
            writeRecordPermit();
        }
    }

    @Transactional
    public Map<String, Object> updateVirtExt(String extensionId, String displayName, String virPass,
            int outPermit, boolean recordpermit,boolean isRoom ,String rezerve_1, String rezerve_2) {
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
        viModel.setVirPass(virPass);
        viModel.setOutPermit(outPermit);
        viModel.setRezerve1(rezerve_1);
        viModel.setRezerve2(rezerve_2);
        viModel.setRecordPermit(recordpermit);
        viModel.setRoom(isRoom);

        PjsipEndpoint endpoint = endpointOpt.get();
        endpoint.setAorsId(extensionId);
        endpoint.setAuthId(extensionId);
        endpoint.setType("endpoint");
        endpoint.setTransport("udp");
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
        roomService.addRoom(extensionId, isRoom);

        writeAutoForvarding();
        writeoutPermit();
        writeRecordPermit();
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

        roomService.deleteRoom(extensionId);
        result.put("success", true);
        result.put("deletedCount", deletedCount);
        writeAutoForvarding();
        writeoutPermit();
        writeRecordPermit();
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

    private void writeAutoForvarding() {
        if (FORWARDING_CONF.exists())
            FORWARDING_CONF.delete();

        try {
            FORWARDING_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FORWARDING_CONF, false))) {
            writer.write("\n[default]\n\n");

            for (ExtenViModel ex : extenVirtualRepo.findAll()) {
                if ((ex.getRezerve1() != null && !ex.getRezerve1().equals(""))
                        || (ex.getRezerve2() != null && !ex.getRezerve2().equals(""))) {
                    writer.write("exten => " + ex.getId() + ",1,NoOp(Primary call attempt to " + ex.getId() + ")\n");
                    writer.write(" same => n,GoSub(permitrecording,s,1(" + ex.getId() + "))\n");
                    writer.write(" same => n,Dial(PJSIP/" + ex.getId() + ",30,T)\n");
                    writer.write(
                            " same => n,GotoIf($[\"${DIALSTATUS}\" = \"BUSY\" || \"${DIALSTATUS}\" = \"NOANSWER\" || \"${DIALSTATUS}\" = \"UNAVAIL\" || \"${DIALSTATUS}\" = \"CHANUNAVAIL\" || \"${DIALSTATUS}\" = \"CONGESTION\"]?fallback1)\n");
                    writer.write(" same => n,Hangup()\n\n");
                    if (ex.getRezerve1() != null && !ex.getRezerve1().equals("")) {
                        if (extenVirtualRepo.existsById(ex.getRezerve1())) {
                            writer.write("exten => " + ex.getId() + ",n(fallback1),NoOp(Forwarding to "
                                    + ex.getRezerve1() + " as first reserve)\n");
                            writer.write(" same => n,Dial(PJSIP/" + ex.getRezerve1() + ",30)\n");
                            if (ex.getRezerve2() != null && !ex.getRezerve2().equals("")) {
                                writer.write(
                                        " same => n,GotoIf($[\"${DIALSTATUS}\" = \"BUSY\" || \"${DIALSTATUS}\" = \"NOANSWER\" || \"${DIALSTATUS}\" = \"UNAVAIL\" || \"${DIALSTATUS}\" = \"CHANUNAVAIL\" || \"${DIALSTATUS}\" = \"CONGESTION\"]?fallback2)\n");
                            }
                            writer.write(" same => n,Hangup()\n\n");
                        } else {
                            writer.write("exten => " + ex.getId() + ",n(fallback1),NoOp(Forwarding to out)\n");
                            writer.write(" same => n,Goto(" + ex.getRezerve1() + ",1)\n\n");
                        }
                    } else {
                        if (extenVirtualRepo.existsById(ex.getRezerve2())) {
                            writer.write("exten => " + ex.getId() + ",n(fallback1),NoOp(Forwarding to "
                                    + ex.getRezerve2() + " as first reserve)\n");
                            writer.write(" same => n,Dial(PJSIP/" + ex.getRezerve2() + ",30)\n");
                            writer.write(" same => n,Hangup()\n\n");
                        } else {
                            writer.write("exten => " + ex.getId() + ",n(fallback1),NoOp(Forwarding to out)\n");
                            writer.write(" same => n,Goto(" + ex.getRezerve2() + ",1)\n\n");
                        }
                    }
                    if (ex.getRezerve2() != null && !ex.getRezerve2().equals("") && ex.getRezerve1() != null
                            && !ex.getRezerve1().equals("")) {
                        if (extenVirtualRepo.existsById(ex.getRezerve2())) {
                            writer.write("exten => " + ex.getId() + ",n(fallback2),NoOp(Forwarding to "
                                    + ex.getRezerve2() + " as second reserve)\n");
                            writer.write(" same => n,Dial(PJSIP/" + ex.getRezerve2() + ",30)\n");
                            writer.write(" same => n,Hangup()\n");
                        } else {
                            writer.write("exten => " + ex.getId() + ",n(fallback2),NoOp(Forwarding to out)\n");
                            writer.write(" same => n,Goto(" + ex.getRezerve2() + ",1)\n\n");
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        asteriskManager.reloadDialplan();
    }

    private void writeoutPermit() {
        if (PERMIT_CONF.exists())
            PERMIT_CONF.delete();

        try {
            PERMIT_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PERMIT_CONF, false))) {
            writer.write("\n[allow-outbound-users]\n");
            writer.write(" exten => _+X.,1,Return()\n");
            writer.write(" exten => _00X.,1,Return()\n");
            for (ExtenViModel ex : extenVirtualRepo.findAll()) {
                if (ex.getOutPermit() == 0)
                    writer.write(" exten =>" + ex.getId() + ",1,Return()\n");
            }
            writer.write("exten => _X.,1,Playback(" + sipSettings.getSysSound() + "/ss-noservice)\n");
            writer.write("same => n,Hangup()\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        asteriskManager.reloadDialplan();
    }

    private void writeRecordPermit() {
        if (PERMIT_RECORDING.exists())
            PERMIT_RECORDING.delete();

        try {
            PERMIT_RECORDING.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PERMIT_RECORDING, false))) {
            writer.write("\n[permitrecording]\n\n");
            writer.write("exten => s,1,NoOp(Start recording if extension matches)\n");

            // ვფილტრავთ მხოლოდ ჩაწერის უფლებით მქონე extensions
            List<ExtenViModel> permittedExtensions = extenVirtualRepo.findAll()
                    .stream()
                    .filter(ExtenViModel::isRecordPermit)
                    .toList();

            if (permittedExtensions.isEmpty()) {
                // თუ არც ერთი არ არის ნებადართული, პირდაპირ ვაბრუნებთ
                writer.write(" same => n,Return()\n");
            } else {
                // ვაწყობთ პირობას
                writer.write(" same => n,ExecIf($[ ");
                for (int i = 0; i < permittedExtensions.size(); i++) {
                    ExtenViModel ext = permittedExtensions.get(i);
                    writer.write("\"${ARG1}\" = \"" + ext.getId() + "\"");
                    if (i < permittedExtensions.size() - 1) {
                        writer.write(" | ");
                    }
                }
                writer.write(
                        " ]?MixMonitor(/var/spool/asterisk/recording/${STRFTIME(${EPOCH},,%Y-%m-%d)}_|_${STRFTIME(${EPOCH},,%k-%M-%S)}_|_${CALLERID(num)}_|_to_|_${ARG1}.wav,b))\n");
                writer.write(" same => n,Return()\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        asteriskManager.reloadDialplan();
    }

}
