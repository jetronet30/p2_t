package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.servermanager.ServerSettings;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SerCon {
    private final ServerSettings serverSettings;

    @PostMapping("/serversettings")
    public String postSerSet(Model m){
        m.addAttribute("port", serverSettings.getPort());
        m.addAttribute("licenzi", serverSettings.getLicenzi());
        m.addAttribute("dataUser", serverSettings.getDataUser());
        m.addAttribute("dataPassword", serverSettings.getDataPassword());
        m.addAttribute("dataPort", serverSettings.getDataPort());
        m.addAttribute("dataName", serverSettings.getDataName());
        m.addAttribute("dataHost", serverSettings.getDataHost());
        return"fragments/serversettings";
    }
    @PostMapping("/set-factory-reset")
    public String factoryRtreset(Model m){
        System.out.println("factoy");
        return"fragments/serversettings";
    }
    @PostMapping("/set-reboot")
    public String setReboot(Model m){
        System.out.println("reboot");
        return"fragments/serversettings";
    }

    @ResponseBody
    @PostMapping("/set-serversettings")
    public Map<String, Object> editServerSettings(@RequestParam("ser-port") int port,
            @RequestParam("ser-licenzi") String licenzi,
            @RequestParam("ser-data-port") int dataPort,
            @RequestParam("ser-data-user") String dataUser,
            @RequestParam("ser-data-password") String dataPassword,
            @RequestParam("ser-data-name") String dataName,
            @RequestParam("ser-data-host") String dataHost) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            serverSettings.editSetting(port, licenzi, dataPort, dataUser, dataPassword, dataName, dataHost);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }


}
