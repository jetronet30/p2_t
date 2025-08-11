package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import com.jaba.p2_t.recorder.RecordService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RecordCon {
    private final RecordService recordService;

    @PostMapping("/callrecording")
    public String posRecording(Model m){
        m.addAttribute("callrecords", recordService.getRecordeFiles());
        for (String cd  : recordService.getUnMountedPartitions()) {
            System.out.println(" UNMOUNTED DISK " + cd);
        }

        
        for (String cd : recordService.getMountedPartitionOnRecording()) {
            System.out.println("MOUNTED DISK " + cd);
        }
        return "fragments/callrecording";
    }

}
