package com.jaba.p2_t.controllers.pmscontrollers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ServiceCodeCon {

    @PostMapping("/servicecodes")
    public String postServiceCodes(){
        return "fragments/servicecodes";
    }

}
