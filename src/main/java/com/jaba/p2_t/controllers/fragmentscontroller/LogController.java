package com.jaba.p2_t.controllers.fragmentscontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class LogController {

    // აქტიური emitter-ების სია
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @PostMapping("/terminal")
    public String postTerminal() {
        return "fragments/terminal";
    }

    // Subscribe for SSE (CLI output)
    @GetMapping(value = "/cli-log", produces = "text/event-stream")
    public SseEmitter subscribeCliLog() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // დავუშვათ ახალი კლიენტი შემოვიდა — მასთვის ვუშვებთ ცალკე Thread-ს
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("asterisk", "-rvvvvvv");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sendSseEvent(line);
                    }
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private void sendSseEvent(String msg) {
        // ANSI escape კოდების ფილტრაცია
        String cleanMsg = msg.replaceAll("\u001B\\[[;\\d]*m", "").replaceAll("\u001B\\[[;\\d]*K", "");

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("cli-line")
                        .data(cleanMsg));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }
}
