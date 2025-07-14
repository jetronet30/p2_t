package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.TrunkService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TrunCon {
    private final TrunkService trunkService;

    @PostMapping("/trunks")
    public String postTrunk(Model m){
        m.addAttribute("trunks", trunkService.getAllTrunk());
        return "fragments/trunks";
    }

    @PostMapping("/trunk-add")
    public String addTrunk( Model m,
                            @RequestParam("login") String login,
                            @RequestParam("password") String password,
                            @RequestParam("server") String server,
                            @RequestParam("fromdomain") String fromdomain,
                            @RequestParam ("qualify")int qualify,
                            @RequestParam("channels") int channels,
                            @RequestParam("forbidden_retry_interval")int forbiddenInterval,
                            @RequestParam("expiration") int expiration,
                            @RequestParam("transport") String transport,
                            @RequestParam("name")String name){
        trunkService.addTrunk(login, password, server,fromdomain, qualify, channels, forbiddenInterval, expiration, transport,name);
        m.addAttribute("trunks", trunkService.getAllTrunk());
        return "fragments/trunks";
    }

    @ResponseBody
    @PostMapping("/delete-trunk/{id}")
    public Map<String, Object>  deleteTrunk(@PathVariable("id") String id){
        
        return trunkService.deleteTrunk(id);
    }

}
