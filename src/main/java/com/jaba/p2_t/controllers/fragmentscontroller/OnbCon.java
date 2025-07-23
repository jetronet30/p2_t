package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OnbCon {

    @PostMapping("/outboundroutes")
    public String postOutBound(){
        return "fragments/outboundroutes";
    }

}
