package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pbxservices.ExtensionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExtCon {
    private final ExtensionService extensionService;

    @PostMapping("/extensions")
    public String postExt(Model model) {
        model.addAttribute("extensions", extensionService.getAllExtensionsSorteId());
        return "fragments/extensions";
    }

    @PostMapping("/ext-add")
    public String addExt(Model model,
            @RequestParam("exten") String exten, @RequestParam("exten-end") String extenEnd) {
        if (extenEnd != null && !extenEnd.isEmpty() && exten != null && !exten.isEmpty()) {
            extensionService.addExtensionsRange(exten, extenEnd);
            //extensionService.generatePjsipConf("/etc/asterisk/pjsip.conf");
        } else if (exten != null && !exten.isEmpty()) {
            extensionService.addExtension(exten);
            //extensionService.generatePjsipConf("/etc/asterisk/pjsip.conf");
        }
        model.addAttribute("extensions", extensionService.getAllExtensionsSorteId());
        return "fragments/extensions";
    }

    @ResponseBody
    @PostMapping("/edit-exten/{id}")
    public Map<String, Object> editExt(
            @PathVariable("id") String id,
            @RequestParam("password") String password,
            @RequestParam("callerId") String callerId,
            @RequestParam("context") String context) {
        try {
            extensionService.editExtension(id, callerId, context, password);
            //extensionService.generatePjsipConf("/etc/asterisk/pjsip.conf");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    @ResponseBody
    @PostMapping("/delete-exten/{id}")
    public Map<String, Object> deleteExt(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        boolean success = extensionService.deleteExtension(id);
        //extensionService.generatePjsipConf("/etc/asterisk/pjsip.conf");
        if (success) {
            res.put("success", true);
        } else {
            res.put("success", false);
            res.put("error", "წაშლა ვერ მოხერხდა");
        }
        return res;
    }
}
