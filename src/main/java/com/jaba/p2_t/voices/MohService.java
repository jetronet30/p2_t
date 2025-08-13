package com.jaba.p2_t.voices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MohService {
    private static final File MOH_FOLDER = new File("/var/lib/asterisk/moh");
    private static final File SETED_MOH = new File("/var/lib/asterisk/moh/play_moh");
    private static final File MOH_CONF = new File("/etc/asterisk/musiconhold.conf");
    private final AsteriskManager asteriskManager;

    public Map<String, Object> setMusicOnHold(String mohName) {
        Map<String, Object> resp = new HashMap<>();

        try {
            // 1️⃣ გავასუფთავოთ SETED_MOH ფოლდერი
            if (SETED_MOH.exists() && SETED_MOH.isDirectory()) {
                File[] files = SETED_MOH.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            System.err.println("ფაილის წაშლა ვერ მოხერხდა: " + file.getAbsolutePath());
                        }
                    }
                }
            } else {
                SETED_MOH.mkdirs(); // თუ არ არსებობს, შევქმნათ
            }

            // 2️⃣ დავაკოპიროთ შერჩეული ფაილი MOH_FOLDER-დან
            File sourceFile = new File(MOH_FOLDER, mohName);
            File destFile = new File(SETED_MOH, mohName);

            if (!sourceFile.exists()) {
                resp.put("success", false);
                resp.put("message", "ფაილი არ მოიძებნა: " + mohName);
                return resp;
            }

            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 3️⃣ musiconhold.conf განახლება
            if (MOH_CONF.exists()) {
                MOH_CONF.delete();
            }
            MOH_CONF.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(MOH_CONF, true))) {
                writer.write("[default]\n");
                writer.write("mode=files\n");
                writer.write("directory=" + SETED_MOH.getAbsolutePath() + "\n");
                writer.write("random=no\n");
            }

            // 4️⃣ MOH reload
            asteriskManager.reloadMoh();

            resp.put("success", true);
            resp.put("message", "Music on Hold წარმატებით განახლდა: " + mohName);

        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "შეცდომა MOH განახლებაში: " + e.getMessage());
        }

        return resp;
    }

    public List<String> getMohFileNames() {

        List<String> mohNames = new ArrayList<>();

        if (MOH_FOLDER.exists() && MOH_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    MOH_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                String name = file.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0) {
                    name = name.substring(0, dotIndex);
                }
                mohNames.add(name);
            }
        }

        return mohNames;
    }

    public List<String> getMohFiles() {

        List<String> mohNames = new ArrayList<>();

        if (MOH_FOLDER.exists() && MOH_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    MOH_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                mohNames.add(file.getName());
            }
        }

        return mohNames;
    }

    public String getSetedMoh() {
        if (SETED_MOH.exists() && SETED_MOH.isDirectory()) {
            File[] files = SETED_MOH.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null && files.length > 0) {
                return files[0].getName(); // პირველი wav ფაილის სახელი
            }
        }
        return "";
    }

    public Map<String, Object> deleteMoh(String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "ფაილის სახელი ცარიელია");
                return response;
            }

            File targetFile = new File(MOH_FOLDER, fileName);

            if (!targetFile.exists()) {
                response.put("success", false);
                response.put("message", "ფაილი არ არსებობს");
                return response;
            }

            boolean deleted = targetFile.delete();
            if (deleted) {
                response.put("success", true);
                response.put("message", "ფაილი წარმატებით წაიშალა");
            } else {
                response.put("success", false);
                response.put("message", "ფაილის წაშლა ვერ მოხერხდა");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "შეცდომა ფაილის წაშლისას: " + e.getMessage());
        }

        return response;
    }

    @SuppressWarnings("null")
    public void uploadMoh(MultipartFile voiceFile) {
        if (!voiceFile.isEmpty() && voiceFile.getContentType() != null
                && voiceFile.getContentType().contains("audio")) {
            try {
                byte[] buffer = voiceFile.getBytes();
                File soundFile = new File(MOH_FOLDER, voiceFile.getOriginalFilename());
                try (FileOutputStream ouf = new FileOutputStream(soundFile)) {
                    ouf.write(buffer);
                }
                mohTransform(soundFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void mohTransform(File soundFile) {
        try {
            // გარდაქმნილი ფაილის სახელი
            String baseName = soundFile.getName().replaceFirst("[.][^.]+$", "");
            File outputFile = new File(MOH_FOLDER, baseName + ".wav");

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
