package com.jaba.p2_t.servermanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.Comparator;

public class SerManger {

    private static final Logger log = LoggerFactory.getLogger(SerManger.class);
    private static final int PROCESS_TIMEOUT_SECONDS = 30; // exec timeout

    /**
     * Executes a system command with timeout, logs stdout and stderr.
     * 
     * @param cmd Command and args.
     * @return true if exit code == 0, else false.
     */
    private static boolean executeCommand(String... cmd) {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // combine stdout+stderr

        try {
            log.info("Executing command: {}", String.join(" ", cmd));
            Process process = pb.start();

            // Read output asynchronously
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder out = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line).append(System.lineSeparator());
                    }
                    return out.toString();
                } catch (IOException e) {
                    log.error("Error reading process output", e);
                    return "";
                }
            });

            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("Process timeout (>{} sec): {}", PROCESS_TIMEOUT_SECONDS, String.join(" ", cmd));
                return false;
            }

            int exitCode = process.exitValue();
            String output = outputFuture.get(1, TimeUnit.SECONDS).trim();

            if (!output.isEmpty()) {
                log.info("Command output: {}", output);
            }

            if (exitCode == 0) {
                log.info("Command succeeded: {}", String.join(" ", cmd));
                return true;
            } else {
                log.error("Command failed with exit code {}: {}", exitCode, String.join(" ", cmd));
                return false;
            }
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Exception during command execution: {}", String.join(" ", cmd), e);
            return false;
        }
    }

    /**
     * Reboots the system.
     * 
     * @return true on success, false otherwise.
     */
    public static boolean reboot() {
        return executeCommand("reboot");
    }

    /**
     * Shuts down the system immediately.
     * 
     * @return true on success, false otherwise.
     */
    public static boolean shutdown() {
        return executeCommand("shutdown", "now");
    }

    /**
     * Restarts the network by applying netplan config.
     * 
     * @return true on success, false otherwise.
     */
    public static boolean restartNetwork() {
        log.info("Applying network config with 'netplan apply'...");

        try {
            Process applyProcess = new ProcessBuilder("netplan", "apply")
                    .inheritIO() // აჩვენოს ტერმინალში stdout/stderr
                    .start();

            int exitCode = applyProcess.waitFor();

            if (exitCode == 0) {
                log.info("Network configuration applied successfully.");
                return true;
            } else {
                log.error("'netplan apply' failed with exit code: {}", exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error while running 'netplan apply'", e);
            return false;
        }
    }

    /**
     * Deletes folder recursively - useful for factory reset.
     * 
     * @param folderPath absolute or relative path.
     * @return true if folder deleted successfully, false otherwise.
     */
    public static boolean factoryReset(String folderPath) {
        try {
            Path folder = Paths.get(folderPath);
            if (Files.notExists(folder)) {
                log.warn("Factory reset folder does not exist: {}", folderPath);
                return false;
            }
            // Recursive delete
            Files.walk(folder)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted: {}", path);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                            throw new RuntimeException(e);
                        }
                    });
            log.info("Factory reset successful for folder: {}", folderPath);
            return true;
        } catch (Exception e) {
            log.error("Factory reset failed for folder: {}", folderPath, e);
            return false;
        }
    }

    /**
     * Gets the current system time as formatted string.
     * 
     * @return formatted current time "yyyy-MM-dd HH:mm:ss".
     */
    public static String getSystemTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Gets system uptime (pretty format).
     * 
     * @return uptime string or "Unavailable" on error.
     */
    public static String getUptime() {
        try {
            ProcessBuilder pb = new ProcessBuilder("uptime", "-p");
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            process.waitFor(5, TimeUnit.SECONDS);
            return output.isEmpty() ? "Unavailable" : output;
        } catch (Exception e) {
            log.error("Failed to get uptime", e);
            return "Unavailable";
        }
    }


    /**
     * Synchronizes system time using NTP.
     * 
     * @return true if succeeded, false otherwise.
     */
    public static boolean syncTimeWithNTP() {
        return executeCommand("timedatectl", "set-ntp", "true");
    }
}
