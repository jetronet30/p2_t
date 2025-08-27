package com.jaba.p2_t.controllers.pmscontrollers;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaba.p2_t.pbxservices.VirtExtensionsService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class RoomController {
    private final VirtExtensionsService virtExtensionsService;

    @PostMapping("/rooms")
    public String postRooms(Model m) {
        m.addAttribute("extensions",virtExtensionsService.getVirtExts());
        return "fragments/rooms";
    }

    @PostMapping("/roomstatus")
    public String testPost(@RequestParam String room, @RequestParam String status) {
        System.out.println(room+"  test  "+status);
        return "fragments/rooms";
    }



}
