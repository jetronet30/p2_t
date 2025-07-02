package com.jaba.p2_t.controllers.fragmentscontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class TestController {
    @PostMapping("/system-overview")
    public String postMethodName() {

        return "fragments/systemover";
    }

    
    @PostMapping("/testing")
     public Map<String, Object> testing(@RequestParam String t1) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", "test_test".equals(t1));
    System.out.println("Received value: " + t1);
    return response;
}


}
