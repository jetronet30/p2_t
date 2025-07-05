package com.jaba.p2_t.bootinit.pbxboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class Pbxinit {

    private static final Logger log = LoggerFactory.getLogger(Pbxinit.class);

    private static final Path MODULES_PATH = Paths.get("/etc/asterisk/modules.conf");

    private static final String MODULES_CONTENT = """
        [modules]
        autoload=yes

        noload = res_radius.so
        noload = app_radius_client.so
        noload = func_radius.so
        """;

    public static void writeModules() {
        try {
            if (Files.exists(MODULES_PATH)) {
                String currentContent = Files.readString(MODULES_PATH);
                if (currentContent.trim().equals(MODULES_CONTENT.trim())) {
                    log.info("modules.conf already up to date. Skipping write.");
                    return;
                }

                // Backup
                Path backupPath = MODULES_PATH.resolveSibling("modules.conf.bak");
                Files.copy(MODULES_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Backup created: {}", backupPath);
            }

            Files.writeString(MODULES_PATH, MODULES_CONTENT, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("modules.conf updated successfully.");

        } catch (IOException e) {
            log.error("Error writing modules.conf", e);
        }
    }
}
