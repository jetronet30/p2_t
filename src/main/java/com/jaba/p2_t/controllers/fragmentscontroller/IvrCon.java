package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PostMapping("/ivr")
    public String postIvr(Model m){
        m.addAttribute("voiceMesages", announcementService.getVoiceFileNames());
        m.addAttribute("systemSound", sipSettings.getSysSound());
        m.addAttribute("voiceLangs", systemSoundsService.getSoundsFileName());
        return "fragments/ivr";
    }

}
