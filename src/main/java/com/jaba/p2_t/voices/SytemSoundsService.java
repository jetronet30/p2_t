package com.jaba.p2_t.voices;

import java.io.File;
import java.io.FileOutputStream;

import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void upladSoundTar(MultipartFile soundTar) {
        try {
            // 1. შექმენი დროებითი ფაილი, რათა MultipartFile გადაყავდეს ფიზიკურ ფაილად
            File tempFile = File.createTempFile("asterisk-sound-", ".tar");
            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(soundTar.getBytes());
            }

            // 2. ამოიღე ენის კოდი (მაგ. 'fr') ფაილის სახელიდან
            String originalFilename = soundTar.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".tar")) {
                throw new IllegalArgumentException("Invalid file name");
            }

            // მაგ: asterisk-core-sounds-fr-gsm-current.tar → fr
            String[] parts = originalFilename.split("-");
            String lang = null;
            for (int i = 0; i < parts.length; i++) {
                if ("sounds".equals(parts[i]) && i + 1 < parts.length) {
                    lang = parts[i + 1]; // fr
                    break;
                }
            }

            if (lang == null) {
                throw new IllegalArgumentException("Could not determine language code from filename");
            }

            File langDir = new File(SOUNDS, lang);
            if (!langDir.exists()) {
                langDir.mkdirs();
            }

            // 4. ამაარქივე ფაილი შესაბამის ფოლდერში
            ProcessBuilder pb = new ProcessBuilder(
                    "tar", "-xvf", tempFile.getAbsolutePath(), "-C", langDir.getAbsolutePath());
            pb.inheritIO(); // ლოგები კონსოლში დაიბეჭდება
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 5. წაშალე დროებითი ფაილი
            tempFile.delete();

            if (exitCode != 0) {
                throw new RuntimeException("Tar extraction failed with exit code " + exitCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and extract sound tar", e);
        }
    }

}
