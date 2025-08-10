package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.IvrService;
import com.jaba.p2_t.pbxservices.SipSettings;
import com.jaba.p2_t.voices.AnnouncementService;
import com.jaba.p2_t.voices.SystemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IvrCon {
    private final AnnouncementService announcementService;
    private final SystemSoundsService systemSoundsService;
    private final SipSettings sipSettings;
    private final IvrService ivrService;

    @PostMapping("/ivr")
    public String postIvr(Model m) {
        m.addAttribute("voiceMesages", announcementService.getVoiceFileNames());
        m.addAttribute("systemSound", sipSettings.getSysSound());
        m.addAttribute("voiceLangs", systemSoundsService.getSoundsFileName());
        m.addAttribute("ivrs", ivrService.listAllIvr());
        return "fragments/ivr";
    }

    @PostMapping("/ivr-add")
    public String addIvr(Model m, @RequestParam("voiceMessage") String voiceMessage,
            @RequestParam("ivr_menu") String ivrMenu) {
        m.addAttribute("voiceMesages", announcementService.getVoiceFileNames());
        m.addAttribute("systemSound", sipSettings.getSysSound());
        m.addAttribute("voiceLangs", systemSoundsService.getSoundsFileName());
        ivrService.createIvr(voiceMessage, ivrMenu);
        m.addAttribute("ivrs", ivrService.listAllIvr());
        return "fragments/ivr";
    }

    @ResponseBody
    @PostMapping("/delete-ivr/{id}")
    public Map<String, Object> deleteIvr(@PathVariable("id") String id) {
        return ivrService.deleteIvrById(id);
    }

    @ResponseBody
    @PostMapping("/edit-ivr/{id}")
    public Map<String,Object> upDateIvr(@PathVariable("id")String id,
                                        @RequestParam("ivr_welcome_message") String voiceMessage,
                                        @RequestParam("ivr_menu") String ivrMenu){
        return ivrService.updateIvr(id,voiceMessage,ivrMenu);
    }

}
