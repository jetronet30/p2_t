package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.security.AdminService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminCon {
    private final AdminService adminService;

    @PostMapping("/administrator")
    private String postAdmin(Model m) {
        m.addAttribute("admin", adminService.getAdmin());
        return "fragments/administrator";
    }
    @ResponseBody
    @PostMapping("/editadmin")
    public Map<String, Object> editAdmin(Model m,
            @RequestParam("username") String userName,
            @RequestParam("userpassword") String userPassword,
            @RequestParam("repassword") String rePassword) {
        adminService.editAdmin(userName, userPassword,rePassword);
        return adminService.editAdmin(userName, userPassword,rePassword);
    }

}
