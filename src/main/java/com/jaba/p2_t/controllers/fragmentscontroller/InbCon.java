package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.InboundService;
import com.jaba.p2_t.pbxservices.TrunkService;
import com.jaba.p2_t.voices.SystemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class InbCon {
    private final TrunkService trunkService;
    private final InboundService inboundService;
    private final SystemSoundsService sytemSoundsService;

    @PostMapping("/inboundroutes")
    public String postInboundRoutes(Model m){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        m.addAttribute("candidates", inboundService.inboundCandidates());
        m.addAttribute("messages", sytemSoundsService.getVoiceFileNames());
        return "fragments/inboundroutes";
    }
    @ResponseBody
    @PostMapping("/setinboundroute/{id}")
    public Map<String, Object> setInboundRoute(Model m,
                                  @PathVariable("id") String id,
                                  @RequestParam("voiceMessage") String voiceMessage,
                                  @RequestParam("candidate")String candidate) {
        m.addAttribute("trunks", trunkService.getAllTrunk());
        m.addAttribute("candidates", inboundService.inboundCandidates());
        return trunkService.setInboundRoute(id, candidate,voiceMessage);
    }

}
