package com.jaba.p2_t.controllers.maincontrollers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class MainCon {
    @GetMapping("/")
    public String getMain() {
        return "main";
    }
}
