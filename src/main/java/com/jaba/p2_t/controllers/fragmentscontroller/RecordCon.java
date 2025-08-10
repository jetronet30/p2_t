package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.jaba.p2_t.recorder.RecordService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RecordCon {
    private final RecordService recordService;

    @PostMapping("/callrecording")
    public String posRecording(){
        for (String cd  : recordService.getNonRootDisks()) {
            System.out.println(" mounted disk " + cd);
        }
        return "fragments/callrecording";
    }

}
