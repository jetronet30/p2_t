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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;
import com.jaba.p2_t.pbxmodels.QueueModel;
import com.jaba.p2_t.pbxrepos.ExtenVirtualRepo;
import com.jaba.p2_t.pbxrepos.QueueRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepo qRepo;
    private final ExtenVirtualRepo extenVirtualRepo;
    private final AsteriskManager asteriskManager;
    private static final File QUEUES_FILE = new File("/etc/asterisk/queues.conf");
    private static final File QUEUES_DIAL_FILE = new File("/etc/asterisk/queues_dial.conf");

    public List<QueueModel> getAllQueue() {
        return qRepo.findAll();
    }

    public List<String> getQueueSortedById() {
        return qRepo.findAll()
                .stream()
                .map(QueueModel::getId)
                .sorted()
                .toList();
    }

    public void createQueue(String voiceMessage, String members, String strategy, String voicelang) {

        Set<String> validMembers = Arrays.stream(members.split("[,\\s.]+"))

                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty() && extenVirtualRepo.existsById(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // თუ არ არის სწორი წევრები, თავიდანვე ვაბრუნებთ
        if (validMembers.isEmpty())
            return;

        // ახალი CallGroup-ის ID-ს ძებნა
        String availableId = findAvailableQueueId()
                .orElseThrow(() -> new RuntimeException("ყველა CallGroup ID დაკავებულია (100000 - 100900)"));

        // CallGroup ობიექტის შექმნა და იდენტიფიკაციის მინიჭება
        QueueModel queue = new QueueModel();
        queue.setId(availableId); // ID მინიჭება
        queue.setContext("default"); // კონტექსტი
        queue.setVoiceMessage(voiceMessage);
        queue.setStrategy(strategy);
        queue.setVoiceLang(voicelang);

        // წევრების სიაში გადაყვანა
        List<String> memberList = new ArrayList<>(validMembers);
        queue.setMembers(memberList); // წევრების დამატება

        // CallGroup-ის შენახვა რეპოზიტორიუმში
        qRepo.save(queue);
        writeQueueConf();

    }

    public Map<String, Object> deleteQueueById(String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (qRepo.existsById(id)) {
                qRepo.deleteById(id);
                writeQueueConf();
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

    public Map<String, Object> updateQueue(String id, String voiceMessage, String members, String strategy,String voicelang) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<QueueModel> optionalQueue = qRepo.findById(id);

            if (optionalQueue.isEmpty()) {
                response.put("success", false);
                response.put("error", "CallGroup not found with ID: " + id);
                return response;
            }

            QueueModel queue = optionalQueue.get();

            Set<String> validMembers = Arrays.stream(members.split("[,\\s.]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && extenVirtualRepo.existsById(s)) // check valid IDs
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<String> memberList = new ArrayList<>(validMembers);
            queue.setMembers(memberList);

            queue.setVoiceMessage(voiceMessage);

            queue.setStrategy(strategy);
            queue.setVoiceLang(voicelang);

            // Save updated group
            qRepo.save(queue);
            writeQueueConf();

            response.put("success", true);
            response.put("id", queue.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    private Optional<String> findAvailableQueueId() {
        for (int i = 200000; i <= 200900; i++) {
            String id = String.valueOf(i);
            if (!qRepo.existsById(id))
                return Optional.of(id);
        }
        return Optional.empty();
    }

    private void writeQueueConf() {
        if (QUEUES_FILE.exists())
            QUEUES_FILE.delete();
        try {
            QUEUES_FILE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(QUEUES_FILE, true))) {

            for (QueueModel que : qRepo.findAll()) {
                writer.write("\n\n[queue-" + que.getId() + "]\n");
                writer.write("setlanguage = " + que.getVoiceLang() + "\n");
                writer.write("musiconhold = default\n");
                writer.write("strategy = " + que.getStrategy() + "\n");
                writer.write("timeout = 15\n");
                writer.write("retry = 5\n");
                writer.write("maxlen = 0\n");
                writer.write("announce-frequency=30\n");
                writer.write("announce-position=yes\n");
                writer.write("announce-holdtime=no\n");
                writer.write("periodic-announce=queue-periodic-announce\n");
                writer.write("periodic-announce-frequency=30\n");
                writer.write("queue-youarenext=queue-youarenext\n");
                writer.write("queue-thereare=queue-thereare\n");
                writer.write("queue-callswaiting=queue-quantity\n");
                writer.write("queue-thankyou=queue-thankyou\n");
                for (String mem : que.getMembers()) {
                    writer.write("member => PJSIP/" + mem + "\n");
                }
                writer.write("\n\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (QUEUES_DIAL_FILE.exists())
            QUEUES_DIAL_FILE.delete();
        try {
            QUEUES_DIAL_FILE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(QUEUES_DIAL_FILE, true))) {
            writer.write("\n[default]\n\n");
            for (QueueModel que : qRepo.findAll()) {
                writer.write("exten => " + que.getId() + ",1,NoOp(Queue queue-" + que.getId() + "  Call)\n");
                writer.write(" same => n,Answer()\n");
                writer.write(" same => n,Wait(1)\n");
                if (!que.getVoiceMessage().equals(""))
                    writer.write(" same => n,Playback(voicemessages/" + que.getVoiceMessage() + ")\n");
                writer.write(" same => n,Set(CHANNEL(language)=" + que.getVoiceLang() + ")\n");
                writer.write(" same => n,Wait(1)\n");
                writer.write(" same => n,Playback(queue-thankyou)\n");
                writer.write(" same => n,Queue(queue-" + que.getId() + ")\n");
                writer.write(" same => n,Hangup()\n");

            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        asteriskManager.reloadDialplan();
    }

}
