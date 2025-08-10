package com.jaba.p2_t.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RecordService {
    private static final File RECORD_FOLDER = new File("/var/spool/asterisk/recording");

    public List<String> getRecordeFileNames() {
        List<String> fileNames = new ArrayList<>();
        if (RECORD_FOLDER.exists() && RECORD_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    RECORD_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                String name = file.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0) {
                    name = name.substring(0, dotIndex);
                }
                fileNames.add(name);
            }
        }
        return fileNames;
    }

    public List<String> getRecordeFiles() {
        List<String> recodedfiles = new ArrayList<>();
        if (RECORD_FOLDER.exists() && RECORD_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    RECORD_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            for (File file : voiceFiles) {
                recodedfiles.add(file.getName());
            }
        }
        return recodedfiles;
    }

    // ზომის კონტროლი და ძველი ფაილების წაშლა
    public void cleanUpIfStorageFull(long maxSizeMB) {
        if (!RECORD_FOLDER.exists() || !RECORD_FOLDER.isDirectory()) {
            return;
        }

        long folderSizeBytes = FileUtils.sizeOfDirectory(RECORD_FOLDER);
        long maxSizeBytes = maxSizeMB * 1024 * 1024;

        if (folderSizeBytes > maxSizeBytes) {
            Collection<File> files = FileUtils.listFiles(
                    RECORD_FOLDER,
                    new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" }),
                    null);

            List<File> sortedFiles = new ArrayList<>(files);
            sortedFiles.sort((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            for (File file : sortedFiles) {
                if (folderSizeBytes <= maxSizeBytes) {
                    break;
                }
                long fileSize = file.length();
                if (file.delete()) {
                    folderSizeBytes -= fileSize;
                    System.out.println("Deleted: " + file.getName());
                }
            }
        }
    }

    // ყოველ საათში ერთხელ გაშვება (cron = წუთი საათი დღე თვე კვირა)
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledCleanUp() {
        cleanUpIfStorageFull(1000); // 1000 MB ლიმიტი
    }


    //დააბრუნებს  ყველა   დისკს
    public  List<String> getNonRootDisks() {
        List<String> disks = new ArrayList<>();
        String rootDevice = null;

        try {
            // ვიპოვოთ root device
            Process rootProc = Runtime.getRuntime()
                    .exec(new String[] { "bash", "-c", "df / --output=source | tail -n 1" });
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(rootProc.getInputStream()))) {
                rootDevice = reader.readLine().trim();
            }

            // ვიპოვოთ ყველა მონტაჟებული დისკი
            Process allProc = Runtime.getRuntime().exec(
                    new String[] { "bash", "-c", "lsblk -ndo NAME,TYPE | grep disk | awk '{print \"/dev/\"$1}'" });
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(allProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String device = line.trim();
                    if (!device.isEmpty() && (rootDevice == null || !device.equals(rootDevice))) {
                        disks.add(device);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return disks;
    }
}
