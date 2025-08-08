package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.networck.NetService;
import com.jaba.p2_t.pbxservices.SipSettings;
import com.jaba.p2_t.voices.SytemSoundsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SipCon {
    private final SipSettings sipSettings;
    private final NetService netService;
     private final SytemSoundsService soundsService;

    @PostMapping("/sipsettings")
    public String postSip(Model m) {
        m.addAttribute("udpPort", sipSettings.getSipUdpPort());
        m.addAttribute("tcpPort", sipSettings.getSipTcpPort());
        m.addAttribute("tlsPort", sipSettings.getSipTlsPort());
        m.addAttribute("defaultPassword", sipSettings.getDefPassword());
        m.addAttribute("dtmf", sipSettings.getDtmfMode());
        m.addAttribute("bindAddress", sipSettings.getBindAddress());
        m.addAttribute("sysSound", sipSettings.getSysSound());
        m.addAttribute("lans", netService.maplan());
        m.addAttribute("sounds", soundsService.getSoundsFileName());
        return "fragments/sipsettings";
    }

    @ResponseBody
    @PostMapping("/sip-settings-set")
    public Map<String, Object> editSipSettings(Model m,
            @RequestParam("sip-udp-port") int udpPort,
            @RequestParam("sip-tcp-port") int tcpPort,
            @RequestParam("sip-tls-port") int tlsPort,
            @RequestParam("sip-dtmf") String dtmf,
            @RequestParam("sip-bind-addres") String bindAddress,
            @RequestParam("sip-default-password") String defaulPassword,
            @RequestParam("sys-sound") String sysSound) {
        m.addAttribute("udpPort", sipSettings.getSipUdpPort());
        m.addAttribute("tcpPort", sipSettings.getSipTcpPort());
        m.addAttribute("tlsPort", sipSettings.getSipTlsPort());
        m.addAttribute("defaultPassword", sipSettings.getDefPassword());
        m.addAttribute("dtmf", sipSettings.getDtmfMode());
        m.addAttribute("bindAddress", sipSettings.getBindAddress());
        m.addAttribute("sysSound", sipSettings.getSysSound());
        m.addAttribute("lans", netService.maplan());
        m.addAttribute("sounds", soundsService.getSoundsFileName());
        if (sipSettings.editSipSettings(udpPort, tcpPort, tlsPort, defaulPassword, dtmf, bindAddress, sysSound)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return response;
        }
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        return error;

    }

}
