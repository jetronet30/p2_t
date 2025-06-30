package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaba.p2_t.networck.LanConfigWritter;
import com.jaba.p2_t.networck.NetService;

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

        // საჭიროა გადატვირთული net-ის ჩასმა მოდელში
        model.addAttribute("net", netService.getNetModels());

        // აბრუნებს ფრაგმენტს (რომელიც უნდა შეიცავდეს <main id="main-content">)
        return "fragments/networksettings";
    }

    @PostMapping("/net-program")
    public String netProgram(Model m) {
        m.addAttribute("net", netService.getNetModels());
        System.out.println("netProgram");
        return "fragments/networksettings";
    }

    @PostMapping("/net-set-and-reboot")
    public String netReboot(Model m) {
        m.addAttribute("net", netService.getNetModels());
        System.out.println("netReboot");
        return "fragments/networksettings";
    }

}
