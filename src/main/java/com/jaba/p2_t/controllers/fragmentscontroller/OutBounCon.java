package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.jaba.p2_t.pbxservices.OutBoundService;
import com.jaba.p2_t.pbxservices.TrunkService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OutBounCon {
    private final TrunkService trunkService;
    private final OutBoundService outBoundService;
    

    @PostMapping("/outboundroutes")
    public String postOutBound(Model m){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        m.addAttribute("outbounds", outBoundService.listAllOutBoundRoutes());
        return "fragments/outboundroutes";
    }

    @PostMapping("/outbound-add")
    public String addOutBoundRouting(Model m,
                                     @RequestParam ("outboundName") String name,
                                     @RequestParam ("prefix") String prefix,
                                     @RequestParam ("autoAdd") String autoAdd,
                                     @RequestParam ("digits") String digits,
                                     @RequestParam ("trunk") String trunk){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        outBoundService.addOutBondRoute(name, prefix, autoAdd, digits, trunk);
        m.addAttribute("outbounds", outBoundService.listAllOutBoundRoutes());
        return "fragments/outboundroutes";
    }


    @ResponseBody
    @PostMapping("/delete-outbound/{id}")
    public Map<String,Object> deleteOutboundRoute(@PathVariable("id")String id){
        return outBoundService.deletOutBoundRoute(id);
    }


    @ResponseBody
    @PostMapping("/edit-outbound/{id}")
    public Map<String,Object> editOutBoundRouting(Model m,
                                     @PathVariable("id")String id,
                                     @RequestParam ("prefix") String prefix,
                                     @RequestParam ("autoAdd") String autoAdd,
                                     @RequestParam ("digits") String digits,
                                     @RequestParam ("trunk") String trunk){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        return outBoundService.editOutBoundRoute(id, prefix, autoAdd, digits, trunk);
    }



}
