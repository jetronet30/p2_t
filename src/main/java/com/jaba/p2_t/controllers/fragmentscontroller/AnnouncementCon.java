package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jaba.p2_t.voices.AnnouncementService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AnnouncementCon {
    private final AnnouncementService announcementService;

    @PostMapping("/announcements")
    public String postAnnouncements(Model m) {
        m.addAttribute("voiceMessages", announcementService.getVoiceFiles());
        m.addAttribute("recorderExtension", announcementService.getRecorderExtension());
        m.addAttribute("recorderCode", announcementService.getRecordCode());
        return "fragments/announcements";
    }

    @PostMapping("/upload_announcement")
    public String uploadAnnouncements(@RequestParam("announcement") MultipartFile announcement, Model m) {
        m.addAttribute("recorderExtension", announcementService.getRecorderExtension());
        m.addAttribute("recorderCode", announcementService.getRecordCode());
        announcementService.uploadVoiceMessage(announcement);
        m.addAttribute("voiceMessages", announcementService.getVoiceFiles());
        return "fragments/announcements";
    }

    @ResponseBody
    @PostMapping("/announcement/delete/{name}")
    public Map<String, Object> deleteAnnounsement(@PathVariable("name") String name) {
        return announcementService.deleteVoiceFile(name);
    }

    @ResponseBody
    @PostMapping("/announcement/rename/{oldName}")
    public Map<String, Object> renameAnnouncement(
            @PathVariable("oldName") String oldName,
            @RequestParam("newName") String newName) {
        return announcementService.renameVoiceFile(oldName, newName);
    }


    @ResponseBody
    @PostMapping("/setvoice/recorder")
    public Map<String, Object> setVoiceRecorder(
            @RequestParam("recorder_extension") String extension,
            @RequestParam("record_code") String code) {
        return announcementService.setRecoder(extension,code);
    }



}
