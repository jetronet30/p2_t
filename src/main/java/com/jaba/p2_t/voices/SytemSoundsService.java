package com.jaba.p2_t.voices;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SytemSoundsService {
    private static final File VOICE_FOLDER = new File("/var/lib/asterisk/sounds/voicemessages");
    private static final File SOUNDS = new File("/var/lib/asterisk/sounds");

    public List<String> getVoiceFileNames() {
        if (!VOICE_FOLDER.exists()) {
            VOICE_FOLDER.mkdir();
        }

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

    public List<String> getSoundsFileName() {
        List<String> folderNames = new ArrayList<>();

        if (SOUNDS.exists() && SOUNDS.isDirectory()) {
            File[] files = SOUNDS.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        folderNames.add(file.getName());
                    }
                }
            }
        }

        return folderNames;
    }

    public void uploadSoundTar(MultipartFile soundTar) {
        try {
            String originalFilename = soundTar.getOriginalFilename();
            if (originalFilename == null ||
                    !(originalFilename.endsWith(".tar") || originalFilename.endsWith(".tar.gz")
                            || originalFilename.endsWith(".tgz"))) {
                throw new IllegalArgumentException("გთხოვ ატვირთო .tar, .tar.gz ან .tgz არქივი");
            }

            // ენის კოდის ამოღება (მაგ: sounds-fr.tar.gz → fr)
            String[] parts = originalFilename.split("-");
            String lang = null;
            for (int i = 0; i < parts.length; i++) {
                if ("sounds".equals(parts[i]) && i + 1 < parts.length) {
                    lang = parts[i + 1];
                    break;
                }
            }

            if (lang == null || lang.length() != 2) {
                throw new IllegalArgumentException("არქივის სახელში ვერ მოიძებნა ენის კოდი (მაგ: fr)");
            }

            // დროებითი ფაილის შექმნა შესაბამისი გაფართოებით
            String suffix = originalFilename.endsWith(".tar.gz") || originalFilename.endsWith(".tgz") ? ".tar.gz"
                    : ".tar";
            File tempFile = File.createTempFile("asterisk-sound-", suffix);
            soundTar.transferTo(tempFile);

            // ენის დირექტორიის შექმნა
            File langDir = new File("/var/lib/asterisk/sounds", lang);
            if (!langDir.exists() && !langDir.mkdirs()) {
                throw new IOException("ვერ შეიქმნა დირექტორია: " + langDir.getAbsolutePath());
            }

            // არქივის გახსნა
            List<String> command = new ArrayList<>();
            command.add("tar");
            command.add("-xvf");
            command.add(tempFile.getAbsolutePath());
            command.add("-C");
            command.add(langDir.getAbsolutePath());

            // დამატებითი დროშა gz-ფაილებისთვის
            if (suffix.equals(".tar.gz")) {
                command.add(1, "-z"); // არქივის შემდეგვე ჩავსვათ -z
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append("[TAR] ").append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("გახსნა წარუმატებელია (exit code = " + exitCode + "): \n" + outputLog);
            }

            if (!tempFile.delete()) {
                System.err.println("ვერ წაიშალა დროებითი ფაილი: " + tempFile.getAbsolutePath());
            }

            System.out.println("სწრაფად აიტვირთა და გაიხსნა ენის პაკეტი: " + lang);
            System.out.println(outputLog);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ფაილის დამუშავება ვერ მოხერხდა", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "გახსნის პროცესი შეწყდა", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "სისტემური შეცდომა: " + e.getMessage(),
                    e);
        }
    }

}
