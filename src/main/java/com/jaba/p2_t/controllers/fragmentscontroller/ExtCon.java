package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.jaba.p2_t.networck.LanConfigWritter;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class ExtCon {
    private final LanConfigWritter lanConfigWritter;

    @PostMapping("/extensions")
    public String postExt() {
        lanConfigWritter.setLan("LAN7", "192.168.300.200", "192.168.300.1", "8.8.8.8", "8.8.4.4",  "24");
        return "fragments/extensions";
    }


}
