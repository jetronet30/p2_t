package com.jaba.p2_t.recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RecordService {
    private static final File RECORD_FOLDER = new File("/var/spool/asterisk/recording");

    private static final String RECORD_FOLDER_PATH = "/var/spool/asterisk/recording";

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

            // სუფიქსი + ზომა >1KB
            IOFileFilter suffixFilter = new SuffixFileFilter(new String[] { ".wav", ".gsm", ".ulaw" });
            IOFileFilter sizeFilter = FileFilterUtils.sizeFileFilter(1024, true); // true = უფრო მეტი ვიდრე 1024 ბაიტი

            IOFileFilter finalFilter = FileFilterUtils.and(suffixFilter, sizeFilter);

            Collection<File> voiceFiles = FileUtils.listFiles(RECORD_FOLDER, finalFilter, null);

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

    // ყოველ 10 დღეში ერთხელ გაშვება და 400GB ზე მეტის შემთხვევაში დასუფთავება
    @Scheduled(cron = "0 0 0 */10 * *")
    public void scheduledCleanUpLarge() {
        long maxSizeGB = 400;
        long maxSizeBytes = maxSizeGB * 1024L * 1024L * 1024L;
        if (!RECORD_FOLDER.exists() || !RECORD_FOLDER.isDirectory()) {
            return;
        }

        long folderSizeBytes = FileUtils.sizeOfDirectory(RECORD_FOLDER);
        System.out.println("Checking recording folder size: " + (folderSizeBytes / (1024 * 1024 * 1024)) + " GB");

        if (folderSizeBytes > maxSizeBytes) {
            System.out.println("Folder size exceeds 400GB. Cleaning up...");
            cleanUpIfStorageFull(maxSizeGB * 1024); // 400GB to MB
        }
    }

    public List<String> getUnMountedPartitions() {
        List<String> partitions = new ArrayList<>();
        List<String> mountedPartitions = new ArrayList<>();

        try {
            // 1. მონტაჟირებული partition-ების მიღება
            Process mountProc = Runtime.getRuntime()
                    .exec(new String[] { "bash", "-c", "mount | grep '^/dev/' | awk '{print $1}'" });
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(mountProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    mountedPartitions.add(line.trim());
                }
            }

            // 2. ყველა partition და მათი ზომა სრული სახელებით
            Process lsblkProc = Runtime.getRuntime()
                    .exec(new String[] { "bash", "-c", "lsblk -lnpo NAME,SIZE,TYPE | grep part" });
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(lsblkProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        String name = parts[0]; // სრული პარტიციის სახელი, მაგალითად /dev/sda1
                        String size = parts[1];
                        String type = parts[2];
                        if ("part".equals(type)) {
                            if (!mountedPartitions.contains(name)) {
                                partitions.add(name + "-|-" + size);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return partitions;
    }

    public List<String> getMountedPartitionOnRecording() {
        List<String> result = new ArrayList<>();

        try {
            // 1. ვპოულობთ მონტაჟირებულ მოწყობილობას ამ ბმულზე
            Process proc = Runtime.getRuntime().exec(new String[] {
                    "bash", "-c", "findmnt -n -o SOURCE --target " + RECORD_FOLDER_PATH
            });
            String device = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                device = reader.readLine();
            }

            if (device == null || device.isEmpty()) {
                System.out.println("No device mounted on " + RECORD_FOLDER_PATH);
                return result;
            }

            device = device.trim(); // მაგალითად: /dev/sda1

            // 2. შევამოწმოთ, არის თუ არა ეს device Ubuntu-ს root ან სხვად რომელიმე
            // მნიშვნელოვანი fs
            boolean isUbuntuPartition = false;

            Process mountProc = Runtime.getRuntime().exec(new String[] {
                    "bash", "-c", "mount | grep '^/dev/' | awk '{print $1}'"
            });
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(mountProc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String mountedDev = line.trim();
                    if (device.equals(mountedDev)) {
                        // ეს device მონტაჟირებულია Ubuntu-ში, ამიტომ არ დავაბრუნოთ
                        isUbuntuPartition = true;
                        break;
                    }
                }
            }

            if (isUbuntuPartition) {

                return result; // ცარიელი სია
            }

            // 3. ვიღებთ მოცულობას (ზომას) ამ partition-ზე
            Process sizeProc = Runtime.getRuntime().exec(new String[] {
                    "bash", "-c", "lsblk -ndo SIZE " + device
            });
            String size = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(sizeProc.getInputStream()))) {
                size = reader.readLine();
            }

            if (size == null || size.isEmpty()) {
                size = "UnknownSize";
            }

            // 4. ვაბრუნებთ ფორმატში: /dev/sda1-|-465.8G
            result.add(device + "-|-" + size.trim());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
