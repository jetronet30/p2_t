package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;


import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ExtCon {

    @PostMapping("/extensions")
    public String postExt(){
        return "fragments/extensions";
    }

}
