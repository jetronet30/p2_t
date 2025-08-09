package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.CallGroupService;
import com.jaba.p2_t.voices.SystemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CallGroupCon {
    private final CallGroupService callGroupService;
    private final SystemSoundsService voicesService;

    @PostMapping("/callgroups")
    public String postCallGroup(Model m) {
        m.addAttribute("callgroups", callGroupService.getAllCallGroups());
        m.addAttribute("messages", voicesService.getVoiceFileNames());
        return "fragments/callgroups";
    }

    @PostMapping("/callgroup-add")
    public String addCallGroup(Model m,
            @RequestParam(name = "members", required = false) String members,
            @RequestParam(name = "voiceMessage", required = false) String voiceMessage,
            @RequestParam(name = "addStrategy") String strategy) {
        callGroupService.createCallGroup(voiceMessage,members,strategy);
        m.addAttribute("callgroups", callGroupService.getAllCallGroups());
        m.addAttribute("messages", voicesService.getVoiceFileNames());
        return "fragments/callgroups";
    }

    @PostMapping("/delete-callgroup/{id}")
    @ResponseBody
    public Map<String, Object> deleteCallGroup(@PathVariable("id") String id) {
        return callGroupService.deleteById(id);
    }

    @ResponseBody
    @PostMapping("/edit-callgroup/{id}")
    public Map<String,Object> editCallGroup(@PathVariable("id") String id,
            @RequestParam(value = "voiceMessage", required = false) String voiceMessage,
            @RequestParam(value = "context", required = false) String context,
            @RequestParam(value = "members", required = false) String members,
            @RequestParam(name = "editStrategy") String strategy) {
                System.out.println(members);
        return callGroupService.updateCallGroup(id,voiceMessage,members,strategy);
    }

}
