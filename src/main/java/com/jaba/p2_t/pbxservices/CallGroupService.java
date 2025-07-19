package com.jaba.p2_t.pbxservices;

import com.jaba.p2_t.pbxmodels.CallGroup;
import com.jaba.p2_t.pbxrepos.CallGroupRepo;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallGroupService {

    // ფაილის გზამკვლევი
    private static final File CALL_GROUP_CONF = new File("/etc/asterisk/custom_callgroup.conf");

    // რეპოზიტორიუმები
    private final CallGroupRepo callGroupRepo;
    private final ExtenVirtualRepo extenVirtualRepo;

    // ყველა CallGroup-ის ამოღება
    public List<CallGroup> getAllCallGroups() {
        return callGroupRepo.findAll();
    }

    // CallGroup-ის ID-ების ჩამონათვალი
    public List<String> getGroupIdSortedById() {
        return callGroupRepo.findAll()
                .stream()
                .map(CallGroup::getId)
                .sorted()
                .toList();
    }

    // ახალი CallGroup-ის შექმნა
    public void createCallGroup(String voiceMessage, String members, String strategy) {
        // წევრების გადამოწმება და ფილტრაცია: მხოლოდ არსებულ ექსტენშენებს მოვუხმობთ
        Set<String> validMembers = Arrays.stream(members.split("[,\\s.]+")) // ჯერ წევრები იყოფა კომების, ცარიელი
                                                                            // სივრცის ან სხვა სიმბოლოებით
                .filter(Objects::nonNull) // ცარიელი მნიშვნელობების გამორიცხვა
                .map(String::trim) // .trim() - ამოიყვანს ცარიელ ადგილებს
                .filter(s -> !s.isEmpty() && extenVirtualRepo.existsById(s)) // მხოლოდ არსებული ექსტენშენები
                .collect(Collectors.toCollection(LinkedHashSet::new)); // უნიკალური წევრების კოლექცია

        // თუ არ არის სწორი წევრები, თავიდანვე ვაბრუნებთ
        if (validMembers.isEmpty())
            return;

        // ახალი CallGroup-ის ID-ს ძებნა
        String availableId = findAvailableCallGroupId()
                .orElseThrow(() -> new RuntimeException("ყველა CallGroup ID დაკავებულია (100000 - 100900)"));

        // CallGroup ობიექტის შექმნა და იდენტიფიკაციის მინიჭება
        CallGroup group = new CallGroup();
        group.setId(availableId); // ID მინიჭება
        group.setContext("default"); // კონტექსტი
        group.setVoiceMessage(voiceMessage);
        group.setStrategy(strategy);

        // წევრების სიაში გადაყვანა
        List<String> memberList = new ArrayList<>(validMembers);
        group.setMembers(memberList); // წევრების დამატება

        // CallGroup-ის შენახვა რეპოზიტორიუმში
        callGroupRepo.save(group);
        writeConfForCallGroup();


    }

    // CallGroup ID-ის მოძებნა
    private Optional<String> findAvailableCallGroupId() {
        for (int i = 100000; i <= 100900; i++) { // ID-ების სფერო 100000-100900
            String id = String.valueOf(i);
            if (!callGroupRepo.existsById(id)) // თუ ID არ არის დაკავებული
                return Optional.of(id);
        }
        return Optional.empty();
    }

    // CallGroup-ის მოძებნა ID-ის მიხედვით
    public Optional<CallGroup> findById(String id) {
        return callGroupRepo.findById(id);
    }

    public Map<String, Object> deleteById(String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (callGroupRepo.existsById(id)) {
                callGroupRepo.deleteById(id);
                writeConfForCallGroup();
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

    // CallGroup-ის განახლება
    public Map<String, Object> updateCallGroup(String id, String voiceMessage, String members, String strategy) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<CallGroup> optionalGroup = callGroupRepo.findById(id);

            if (optionalGroup.isEmpty()) {
                response.put("success", false);
                response.put("error", "CallGroup not found with ID: " + id);
                return response;
            }

            CallGroup group = optionalGroup.get();

            // Update members (split and filter members string)
            Set<String> validMembers = Arrays.stream(members.split("[,\\s.]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && extenVirtualRepo.existsById(s)) // check valid IDs
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<String> memberList = new ArrayList<>(validMembers);
            group.setMembers(memberList); // Set members

            group.setVoiceMessage(voiceMessage);

            group.setStrategy(strategy);

            // Save updated group
            callGroupRepo.save(group);
            writeConfForCallGroup();



            response.put("success", true);
            response.put("id", group.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private void writeConfForCallGroup() {
        if (CALL_GROUP_CONF.exists())
            CALL_GROUP_CONF.delete();
        try {
            CALL_GROUP_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CALL_GROUP_CONF, true))) {
            writer.write("\n[default]\n\n");
            for (CallGroup cg : callGroupRepo.findAll()) {
                writer.write("exten => " + cg.getId() + ",1,NoOp(CallGroup " + cg.getId() + ")\n");

                // VoiceMessage check
                if (!cg.getVoiceMessage().isEmpty()) writer.write("same => n,Playback(voicemessages/" + cg.getVoiceMessage() + ")\n");

                // Handle strategy-based call flow
                switch (cg.getStrategy()) {
                    case "RingAll":
                        for (String member : cg.getMembers()) {
                            writer.write("same => n,Dial(PJSIP/" + member + ",20)\n");
                        }
                        break;

                    case "RingGroup":
                        writer.write("same => n,Dial(PJSIP/" + String.join(",", cg.getMembers()) + ",g)\n");
                        break;

                    case "FirstAvailable":
                        for (String member : cg.getMembers()) {
                            writer.write("same => n,Dial(PJSIP/" + member + ",20)\n");
                            writer.write("same => n,GotoIf($[${DIALSTATUS} = \"ANSWERED\"]?found:next)\n");
                        }
                        writer.write("same => n(next),Hangup()\n");
                        writer.write("same => n(found),Hangup()\n");
                        break;

                    case "RoundRobin":
                        // Implement round-robin logic (can be tricky to do directly in dialplan)
                        // For simplicity, let's simulate it
                        String[] members = cg.getMembers().toArray(new String[0]);
                        for (int i = 0; i < members.length; i++) {
                            writer.write("same => n,Dial(PJSIP/" + members[i] + ",20)\n");
                        }
                        break;

                    default:
                        writer.write("same => n,Hangup()\n");
                        break;
                }

                writer.write("same => n,Hangup()\n\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
