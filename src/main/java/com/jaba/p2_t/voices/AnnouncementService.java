package com.jaba.p2_t.voices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class AnnouncementService {

    private static final File VOICE_FOLDER = new File("/var/lib/asterisk/sounds/voicemessages");

    @PostConstruct
    public void createVoiceFolder() {
        if (!VOICE_FOLDER.exists())
            VOICE_FOLDER.mkdirs();
    }

    public List<String> getVoiceFileNames() {

        List<String> voiceNames = new ArrayList<>();

        if (VOICE_FOLDER.exists() && VOICE_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    VOICE_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                String name = file.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0) {
                    name = name.substring(0, dotIndex);
                }
                voiceNames.add(name);
            }
        }

        return voiceNames;
    }

    public List<String> getVoiceFiles() {

        List<String> voiceNames = new ArrayList<>();

        if (VOICE_FOLDER.exists() && VOICE_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    VOICE_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                voiceNames.add(file.getName());
            }
        }

        return voiceNames;
    }

    public void uploadVoiceMessage(MultipartFile voiceFile) {
        if (!voiceFile.isEmpty() && voiceFile.getContentType() != null
                && voiceFile.getContentType().contains("audio")) {
            try {
                byte[] buffer = voiceFile.getBytes();
                File soundFile = new File(VOICE_FOLDER, voiceFile.getOriginalFilename());
                try (FileOutputStream ouf = new FileOutputStream(soundFile)) {
                    ouf.write(buffer);
                }
                sound_transform(soundFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sound_transform(File soundFile) {
        try {
            // გარდაქმნილი ფაილის სახელი
            String baseName = soundFile.getName().replaceFirst("[.][^.]+$", "");
            File outputFile = new File(VOICE_FOLDER,  baseName + ".wav");

            // ffmpeg ბრძანება array სახით
            String[] cmd = {
                    "ffmpeg",
                    "-y",
                    "-i", soundFile.getAbsolutePath(),
                    "-ac", "1",
                    "-ar", "8000",
                    "-sample_fmt", "s16",
                    outputFile.getAbsolutePath()
            };

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // stderr და stdout ერთად
            Process process = pb.start();

            // ვკითხულობთ ffmpeg-ის ლოგს
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                if (soundFile.exists() && !soundFile.equals(outputFile)) {
                    soundFile.delete();
                }
            } else {
                System.err.println("FFmpeg გარდაქმნა ვერ შესრულდა! Exit code: " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
