package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.QueueService;
import com.jaba.p2_t.voices.VoicesService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QuCon {
    private final QueueService queueService;
    private final VoicesService voicesService;

    @PostMapping("/queues")
    public String postQueues(Model m){
        m.addAttribute("queues", queueService.getAllQueue());
        m.addAttribute("messages", voicesService.getVoiceFileNames());
        return "fragments/queues";
    }

    @PostMapping("/queue-add")
    public String addQueue(Model m,
            @RequestParam(name = "members", required = false) String members,
            @RequestParam(name = "voiceMessage", required = false) String voiceMessage,
            @RequestParam(name = "addStrategy") String strategy) {
        queueService.createQueue(voiceMessage,members,strategy);
        m.addAttribute("queues", queueService.getAllQueue());
        m.addAttribute("messages", voicesService.getVoiceFileNames());
        return "fragments/callgroups";
    }

    @PostMapping("/delete-queue/{id}")
    @ResponseBody
    public Map<String, Object> deleteQueue(@PathVariable("id") String id) {
        return queueService.deleteQueueById(id);
    }

    @ResponseBody
    @PostMapping("/edit-queue/{id}")
    public Map<String,Object> editCallGroup(@PathVariable("id") String id,
            @RequestParam(value = "voiceMessage", required = false) String voiceMessage,
            @RequestParam(value = "context", required = false) String context,
            @RequestParam(value = "members", required = false) String members,
            @RequestParam(name = "editStrategy") String strategy) {
                System.out.println(members);
        return queueService.updateQueue(id,voiceMessage,members,strategy);
    }


}
