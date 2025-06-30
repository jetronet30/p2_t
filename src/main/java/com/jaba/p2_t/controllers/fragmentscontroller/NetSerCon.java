package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaba.p2_t.networck.LanConfigWritter;
import com.jaba.p2_t.networck.NetService;
import com.jaba.p2_t.servermanager.SerManger;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NetSerCon {
    private final NetService netService;
    private final LanConfigWritter lanConfigWritter;

    @PostMapping("/network-settings")
    public String getNet(Model m) {
        m.addAttribute("net", netService.getNetModels());
        return "fragments/networksettings";
    }

    @PostMapping("/set-lan")
    public String setLan(@RequestParam String nickname,
            @RequestParam String ip,
            @RequestParam String gateway,
            @RequestParam(required = false) String dns1,
            @RequestParam(required = false) String dns2,
            @RequestParam String subnet,
            @RequestParam String metric,
            Model model) {

        lanConfigWritter.setLan(nickname, ip, gateway, dns1, dns2, subnet, metric);

        model.addAttribute("net", netService.getNetModels());

        return "fragments/networksettings";
    }

    @PostMapping("/net-set-and-reboot")
    public String netReboot(Model m) {
        if (SerManger.restartNetwork())
            SerManger.reboot();
        return "fragments/networksettings";
    }

}
