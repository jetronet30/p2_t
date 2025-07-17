package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.jaba.p2_t.pbxservices.InboundService;
import com.jaba.p2_t.pbxservices.TrunkService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class InbCon {
    private final TrunkService trunkService;
    private final InboundService inboundService;

    @PostMapping("/inboundroutes")
    public String postInboundRoutes(Model m){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        m.addAttribute("candidates", inboundService.inboundCandidates());
        return "fragments/inboundroutes";
    }

    @PostMapping("/setinboundroute/{id}")
    public String setInboundRoute(Model m,
                                  @PathVariable("id") String id){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        return "fragments/inboundroutes";
    }

}
