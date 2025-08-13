package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jaba.p2_t.voices.MohService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MohCon {
    private final MohService mohService;

    @PostMapping("/moh")
    public String postMoh(Model m){
        m.addAttribute("mohs", mohService.getMohFiles());
        m.addAttribute("setedMoh", mohService.getSetedMoh());
        return "fragments/moh";
    }

    @PostMapping("/upload_moh")
    public String uploadMoh(@RequestParam("musiconhold") MultipartFile musiconhold, Model m) {
        mohService.uploadMoh(musiconhold);
        m.addAttribute("mohs", mohService.getMohFiles());
        m.addAttribute("setedMoh", mohService.getSetedMoh());
        return "fragments/moh";
    }

    @ResponseBody
    @PostMapping("/moh/delete/{name}")
    public Map<String, Object> deleteMoh(@PathVariable("name") String name) {
        return mohService.deleteMoh(name);
    }

    @ResponseBody
    @PostMapping("/set-music-on-hold")
    public Map<String, Object> setMoh(@RequestParam("select-moh") String name) {
        System.out.println(name);
        return mohService.setMusicOnHold(name);
    }


}
