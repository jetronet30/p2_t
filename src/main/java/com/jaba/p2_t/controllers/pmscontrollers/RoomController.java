package com.jaba.p2_t.controllers.pmscontrollers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.jaba.p2_t.pms.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // აქტიური emitter-ების სია
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @PostMapping("/rooms")
    public String postRooms(Model m) {
        m.addAttribute("extensions", roomService.listRooms());
        return "fragments/rooms";
    }

    // Subscribe for SSE
    @GetMapping("/rooms/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    @PostMapping("/roomstatus")
    public String setStatust(@RequestParam String room, @RequestParam String status) {
        roomService.setStatus(room, status);
        System.out.println(room + "  test  " + status);

        // გაუგზავნე ყველა subscribed კლიენტს
        sendSseEvent(room + " changed to " + status);

        return "fragments/rooms";
    }

    private void sendSseEvent(String msg) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("room-status")   // event name
                        .data(msg));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }
}
