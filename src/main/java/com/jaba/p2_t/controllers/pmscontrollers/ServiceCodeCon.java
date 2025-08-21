package com.jaba.p2_t.controllers.pmscontrollers;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaba.p2_t.pms.ServiceCodeService;

import lombok.RequiredArgsConstructor;

import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class ServiceCodeCon {
    private final ServiceCodeService scs;

    @PostMapping("/servicecodes")
    public String postServiceCodes(Model m){
        m.addAttribute("code", scs.getServiceCode());
        return "fragments/servicecodes";
    }

    @ResponseBody
    @PostMapping("/set-servicecodes")
    public Map<String, Object> setCodes(@RequestParam("code-clean") String clean,
                                        @RequestParam("code-dirty") String dirty,
                                        @RequestParam("code-ofo") String ofo,
                                        @RequestParam("code-ofs") String ofs,
                                        @RequestParam("code-inspected") String inspected,
                                        @RequestParam("code-alarm") String alarm){
        return scs.editServiceCodes(clean, dirty, ofo, ofs, inspected, alarm);
    }

}
