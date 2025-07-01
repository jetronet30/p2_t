package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.jaba.p2_t.extensions.ExtenService;
import com.jaba.p2_t.extensions.Extensions;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExtCon {
    private final ExtenService eService;

    @PostMapping("/extensions")
    public String postExt() {
        Extensions ext = new Extensions();
        ext.setExten("1002"); // აუცილებელია!
        ext.setContext("default");
        ext.setPriority(1);
        ext.setApplication("Dial");
        ext.setAppData("PJSIP/1001");
        ext.setDescription("Office phone");
        eService.save(ext);
        System.out.println(eService.findAll().size());
        return "fragments/extensions";
    }

}
