package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.QueueService;
import com.jaba.p2_t.pbxservices.SipSettings;
import com.jaba.p2_t.voices.SytemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QuCon {
    private final QueueService queueService;
    private final SytemSoundsService sytemSoundsService;
    private final SipSettings sipSettings;

    @PostMapping("/queues")
    public String postQueues(Model m){
        m.addAttribute("queues", queueService.getAllQueue());
        m.addAttribute("messages", sytemSoundsService.getVoiceFileNames());
        m.addAttribute("sounds", sytemSoundsService.getSoundsFileName());
        m.addAttribute("systemSound", sipSettings.getSysSound());
        return "fragments/queues";
    }

    @PostMapping("/queue-add")
    public String addQueue(Model m,
            @RequestParam(name = "members", required = false) String members,
            @RequestParam(name = "voiceMessage", required = false) String voiceMessage,
            @RequestParam(name = "queue_strategy") String strategy,
            @RequestParam(name = "voiceLang") String voiceLang) {
        queueService.createQueue(voiceMessage,members,strategy,voiceLang);
        m.addAttribute("queues", queueService.getAllQueue());
        m.addAttribute("messages", sytemSoundsService.getVoiceFileNames());
        m.addAttribute("sounds", sytemSoundsService.getSoundsFileName());
        m.addAttribute("systemSound", sipSettings.getSysSound());
        return "fragments/queues";
    }

    @ResponseBody
    @PostMapping("/delete-queue/{id}")
    public Map<String, Object> deleteQueue(@PathVariable("id") String id) {
        return queueService.deleteQueueById(id);
    }

    @ResponseBody
    @PostMapping("/edit-queue/{id}")
    public Map<String,Object> editQueue(@PathVariable("id") String id,
            @RequestParam(value = "queueVoiceMessage", required = false) String voiceMessage,
            @RequestParam(value = "context", required = false) String context,
            @RequestParam(value = "queue_members", required = false) String members,
            @RequestParam(name = "editStrategy") String strategy,
            @RequestParam(name = "voiceLang") String voiceLang) {
                System.out.println(members);
        return queueService.updateQueue(id,voiceMessage,members,strategy,voiceLang);
    }


}
