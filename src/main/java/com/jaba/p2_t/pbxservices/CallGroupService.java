package com.jaba.p2_t.pbxservices;

import com.jaba.p2_t.pbxmodels.CallGroup;
import com.jaba.p2_t.pbxrepos.CallGroupRepo;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallGroupService {

    // ფაილის გზამკვლევი
    private final Path EXTEN_FILE = Paths.get("/etc/asterisk/extensions.conf");

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

        // გაფორმება extensions.conf ფაილში
        writeExtensionById(group);
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

    // CallGroup-ის წაშლა ID-ის მიხედვით
    public Map<String, Object> deleteById(String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (callGroupRepo.existsById(id)) {
                callGroupRepo.deleteById(id); // წაშლა რეპოზიტორიუმიდან
                response.put("success", true);
                deleteExtensionById(id); // extensions.conf ფაილიდან წაშლა
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

    private void writeExtensionById(CallGroup group) {
        try {
            if (group == null || group.getId() == null)
                return;

            List<String> existingLines = Files.exists(EXTEN_FILE)
                    ? Files.readAllLines(EXTEN_FILE)
                    : new ArrayList<>(); // ამოღება არსებული ხაზებისგან

            List<String> result = new ArrayList<>();
            String id = group.getId();

            boolean insideBlock = false;
            for (String line : existingLines) {
                String trimmed = line.trim();
                if (trimmed.equals("; Call Group start " + id)) {
                    insideBlock = true; // Block-ის შიგნით გასვლა
                    continue; // არ დაამატოთ ეს ხაზი შედეგში
                }
                if (insideBlock) {
                    if (trimmed.equals("; Call Group end " + id)) {
                        insideBlock = false; // Block-ის დასასრული
                    }
                    continue; // გამოტოვეთ ყველა ხაზი შიგნით
                }
                result.add(line); // დაამატეთ სხვა ხაზები
            }

            // თუ წევრები არ არის ცარიელი, დაამატეთ ახალი CallGroup
            List<String> members = group.getMembers();
            if (!members.isEmpty()) {
                result.add(""); // გამოტოვება
                result.add("; Call Group start " + id);
                result.add("exten => " + id + ",1,NoOp(CallGroup " + id + ")");

                String voiceMessage = group.getVoiceMessage();
                if (voiceMessage != null && !voiceMessage.isBlank()) {
                    result.add(" same => n,Playback(voicemessages/" + voiceMessage + ")");
                }

                // სტრატეგიის პარამეტრის შემოწმება
                String strategy = group.getStrategy(); // Get the strategy (covered or paralel)
                if ("covered".equalsIgnoreCase(strategy)) {
                    // Coverd strategy: Call sequentially
                    for (String member : members) {
                        result.add(" same => n,Dial(PJSIP/" + member + ",20)");
                    }
                } else if ("paralel".equalsIgnoreCase(strategy)) {
                    // Parallel strategy: Dial all members at once
                    result.add(" same => n,Dial(" +
                            members.stream().map(m -> "PJSIP/" + m).collect(Collectors.joining("&")) +
                            ",20)"); // Parallel dialing
                } else {
                    // Default behavior (fallback)
                    result.add(" same => n,Dial(" +
                            members.stream().map(m -> "PJSIP/" + m).collect(Collectors.joining("&")) +
                            ",20)"); // Default strategy, no `c` option
                }

                result.add(" same => n,Hangup()");
                result.add("; Call Group end " + id);
            }

            // ფაილის წერილი
            Files.write(EXTEN_FILE, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CallGroup-ის წაშლა extensions.conf ფაილიდან
    private void deleteExtensionById(String id) {
        try {
            if (id == null || !Files.exists(EXTEN_FILE))
                return;

            List<String> lines = Files.readAllLines(EXTEN_FILE);
            List<String> result = new ArrayList<>();
            boolean insideBlock = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.equals("; Call Group start " + id)) {
                    insideBlock = true;
                    continue;
                }
                if (insideBlock) {
                    if (trimmed.equals("; Call Group end " + id)) {
                        insideBlock = false;
                        continue;
                    }
                    continue;
                }
                result.add(line);
            }

            Files.write(EXTEN_FILE, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            // Update voiceMessage
            group.setVoiceMessage(voiceMessage);

            group.setStrategy(strategy);

            // Save updated group
            callGroupRepo.save(group);

            // Update the extensions.conf file
            writeExtensionById(group);

            response.put("success", true);
            response.put("id", group.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
