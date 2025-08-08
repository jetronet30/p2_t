package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jaba.p2_t.voices.AnnouncementService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AnnouncementCon {
    private final AnnouncementService announcementService;

    @PostMapping("/announcements")
    public String postAnnouncements(Model m){
        m.addAttribute("voiceMessages", announcementService.getVoiceFiles());
        return "fragments/announcements";
    }

    @PostMapping("/upload_announcement")
    public String uploadAnnouncements(@RequestParam("announcement") MultipartFile announcement, Model m){
        announcementService.uploadVoiceMessage(announcement);
        m.addAttribute("voiceMessages", announcementService.getVoiceFiles());
        return "fragments/announcements";
    }

}
