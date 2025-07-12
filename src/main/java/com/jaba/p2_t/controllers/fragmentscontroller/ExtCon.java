package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.jaba.p2_t.pbxservices.VirtExtensionsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExtCon {
    private final VirtExtensionsService virtExtensionsService;

    @PostMapping("/extensions")
    public String postExt(Model model) {
        virtExtensionsService.updateUserIp();
        model.addAttribute("extensions", virtExtensionsService.getVirtExts());
        return "fragments/extensions";
    }

    @PostMapping("/ext-add")
    public String addExt(Model model,
            @RequestParam("exten") String exten, @RequestParam("exten-end") String extenEnd) {
        if (extenEnd != null && !extenEnd.isEmpty() && exten != null && !exten.isEmpty()) {
            virtExtensionsService.createVirtExtInRange(exten, extenEnd);
        } else if (exten != null && !exten.isEmpty()) {
            virtExtensionsService.createVirtExt(exten);
        }
        model.addAttribute("extensions", virtExtensionsService.getVirtExts());
        return "fragments/extensions";
    }

    @ResponseBody
    @PostMapping("/edit-exten/{id}")
    public Map<String, Object> editExt(
            @PathVariable("id") String id,
            @RequestParam("password") String password,
            @RequestParam("callerId") String callerId,
            @RequestParam("context") String context) {
        return virtExtensionsService.updateVirtExt(id,callerId,context,password,3);
    }

    @ResponseBody
    @PostMapping("/delete-exten/{id}")
    public Map<String, Object> deleteExt(@PathVariable("id") String id) {
       return virtExtensionsService.deleteVirtExt(id);
    }
}
