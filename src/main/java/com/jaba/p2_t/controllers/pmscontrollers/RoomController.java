package com.jaba.p2_t.controllers.pmscontrollers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class RoomController {

    @PostMapping("/rooms")
    public String postRooms() {
        return "fragments/rooms";
    }

    @PostMapping("/roomstatus")
    public String testPost(@RequestParam String room, @RequestParam String status) {
        System.out.println(room+"  test  "+status);
        return "fragments/rooms";
    }



}
