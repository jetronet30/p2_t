package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.pbxmodels.IvrModel;
import com.jaba.p2_t.pbxrepos.CallGroupRepo;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import com.jaba.p2_t.pbxrepos.IvrRepo;
import com.jaba.p2_t.pbxrepos.QueueRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IvrService {
    private final IvrRepo ivrRepo;
    private final ExtenVirtualRepo extenVirtualRepo;
    private final QueueRepo queueRepo;
    private final CallGroupRepo callGroupRepo;

    private static final File IVR_CONF = new File("/etc/asterisk/ivr.conf");

    public void createIvr(String voiceMessage, String members) {

        // ციფრების ამოღება (მაგ. "1=Sales, 2=Support" → [1, 2])
        List<Integer> digitList = Arrays.stream(members.split("[,\\s]+"))
                .map(s -> s.split("=")[0]) // ვიღებთ მხოლოდ ციფრს
                .map(String::trim)
                .filter(s -> s.matches("\\d+")) // ვტოვებთ მხოლოდ ციფრებს
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        // წევრების ამოღება და ვალიდაცია
        Set<String> validMembers = Arrays.stream(members.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> s.contains("=")) // მხოლოდ "digit=Name" ფორმატები
                .map(s -> s.split("=")[1]) // ვიღებთ მხოლოდ სახელს
                .map(String::trim)
                .filter(s -> !s.isEmpty() && (extenVirtualRepo.existsById(s) ||
                        queueRepo.existsById(s) ||
                        callGroupRepo.existsById(s) ||
                        ivrRepo.existsById(s)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // თუ არ არის სწორი წევრები, თავიდანვე ვაბრუნებთ
        if (validMembers.isEmpty())
            return;

        // თავისუფალი ID-ის პოვნა
        String availableId = findAvailableIvrId()
                .orElseThrow(() -> new RuntimeException("ყველა CallGroup ID დაკავებულია (300000 - 300900)"));

        // ახალი IvrModel ობიექტის შექმნა
        IvrModel ivr = new IvrModel();
        ivr.setId(availableId);
        ivr.setContext("default");
        ivr.setVoiceMessage(voiceMessage);
        ivr.setMembers(new ArrayList<>(validMembers));
        ivr.setDigits(digitList); // ციფრების დამატება

        // შენახვა
        ivrRepo.save(ivr);
        writeIvrConf();
    }

    public Map<String, Object> deleteIvrById(String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ivrRepo.existsById(id)) {
                ivrRepo.deleteById(id);
                writeIvrConf();
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "ID not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    public Map<String, Object> updateIvr(String id, String voiceMessage, String members) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<IvrModel> optionalIvr = ivrRepo.findById(id);

            if (optionalIvr.isEmpty()) {
                response.put("success", false);
                response.put("error", "IVR not found with ID: " + id);
                return response;
            }

            IvrModel ivr = optionalIvr.get();

            // ციფრების ამოღება
            List<Integer> digitList = Arrays.stream(members.split("[,\\s]+"))
                    .map(s -> s.split("=")[0])
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            // წევრების ამოღება და ვალიდაცია
            Set<String> validMembers = Arrays.stream(members.split("[,\\s]+"))
                    .map(String::trim)
                    .filter(s -> s.contains("="))
                    .map(s -> s.split("=")[1])
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && (extenVirtualRepo.existsById(s) ||
                            queueRepo.existsById(s) ||
                            callGroupRepo.existsById(s) ||
                            ivrRepo.existsById(s)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // თუ ცარიელია — შეცდომა
            if (validMembers.isEmpty()) {
                response.put("success", false);
                response.put("error", "No valid members found.");
                return response;
            }

            // განახლება
            ivr.setVoiceMessage(voiceMessage);
            ivr.setDigits(digitList);
            ivr.setMembers(new ArrayList<>(validMembers));

            // შენახვა
            ivrRepo.save(ivr);
            writeIvrConf();

            response.put("success", true);
            response.put("id", ivr.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private Optional<String> findAvailableIvrId() {
        for (int i = 300000; i <= 300900; i++) {
            String id = String.valueOf(i);
            if (!ivrRepo.existsById(id))
                return Optional.of(id);
        }
        return Optional.empty();
    }
    @PostConstruct
    public void writeIvrConf() {
        if (IVR_CONF.exists())
            IVR_CONF.delete();
        try {
            IVR_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(IVR_CONF, true))) {

            writer.write("\n[default]\n");

            // ყველა IVR ჩამოტვირთვა
            List<IvrModel> ivrs = ivrRepo.findAll();
            for (IvrModel ivr : ivrs) {
                writer.write("exten => "+ivr.getId()+",1,Goto(ivr-menu-"+ivr.getId()+",s,1)\n");
            }

            for (IvrModel ivr : ivrs) {
                writer.write("\n[ivr-menu-" + ivr.getId() + "]\n");
                writer.write("exten => s,1,Answer()\n");
                writer.write(" same => n(start),Background(voicemessages/" + ivr.getVoiceMessage() + ")\n");
                writer.write(" same => n,WaitExten(5)\n");
                writer.write(" same => n,Goto(start)\n\n");

                List<Integer> digits = ivr.getDigits();
                List<String> members = ivr.getMembers();

                if (digits != null && members != null && digits.size() == members.size()) {
                    for (int i = 0; i < digits.size(); i++) {
                        int digit = digits.get(i);
                        String member = members.get(i);
                        writer.write("exten => " + digit + ",1,Goto(default," + member + ",1)\n");
                        writer.write(" same => n,Hangup()\n");
                    }
                }

                writer.write("exten => i,1,Playback(invalid)\n");
                writer.write(" same => n,Goto(s,start)\n\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
