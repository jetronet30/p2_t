package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jaba.p2_t.voices.SytemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SystemSoundsCon {
    private final SytemSoundsService soundsService;

    @PostMapping("/systemsounds")
    public String postSystemSounds(Model m){
        m.addAttribute("sounds", soundsService.getSoundsFileName());
        return "fragments/systemsounds";
    }

    @PostMapping("/upload_system_sound")
    public String uploadSysSound(@RequestParam("systemsound") MultipartFile sysSound){
        soundsService.uploadSoundTar(sysSound);
        return "fragments/systemsounds";
    }

}
