package com.jaba.p2_t.bootinit.ffmpeg;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;



@Service
public class FFmpegManager {

    public static void ffmpegInit() {
        if (!isFfmpegInstalled()) {
            installFfmpegOnline();
        }
    }

    private static boolean isFfmpegInstalled() {
        try {
            Process process = new ProcessBuilder("which", "ffmpeg").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                return output != null && !output.isBlank();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean installFfmpegOnline() {
        try {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
                    "sudo apt update && sudo apt install -y ffmpeg");
            builder.inheritIO();
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("FFmpeg installed successfully.");
                return true;
            } else {
                System.err.println("Failed to install FFmpeg. Exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
