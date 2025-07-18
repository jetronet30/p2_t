package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CallGroupCon {
    @PostMapping("/callgroups")
    public String postCallGroup() {
        return "fragments/callgroups";
    }

    @PostMapping("/callgroup-add")
    public String addCallGroup(@RequestParam Map<String, String> params, Model model) {
        for (int i = 1; i <= 10; i++) {
            String member = params.get("member" + i);
            System.out.println("Member" + i + ": " + member);
        }
        return "fragments/callgroups";
    }

}
