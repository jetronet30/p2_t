package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

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

}
