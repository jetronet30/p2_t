package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SerCon {
    @PostMapping("/server-settings")
    public String postSerSet(Model m){
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
    public Map<String, Object> editServerSettings() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            System.out.println("AAAAAAAAAAAA");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            System.out.println("MMMMMMMMMMMMM");
            return error;
        }
    }


}
