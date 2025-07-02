package com.jaba.p2_t.bootinit;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepoInit {
    private static final Logger LOGGER = Logger.getLogger(RepoInit.class.getName());

    public static final File SERVER_REPO = new File("./mainrepo");
    //public static final File SERVER_REPO = new File("/var/lib/mainrepo");
    public static final File SERVER_RESOURCES = new File("./mainrepo/resources");
    //public static final File SERVER_RESOURCES = new File("/var/lib/mainrepo/resources");
    public static final File REZERV_REPO = new File("/var/lib/rezerv");

    public static void repoCreator() {
        createDirectory(SERVER_REPO);
        createDirectory(SERVER_RESOURCES);
        createDirectory(REZERV_REPO);
    }

    private static void createDirectory(File dir) {
        try {
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    LOGGER.info("Directory created: " + dir.getAbsolutePath());
                } else {
                    LOGGER.warning("Failed to create directory: " + dir.getAbsolutePath());
                }
            } else if (!dir.isDirectory()) {
                LOGGER.severe("Path exists but is not a directory: " + dir.getAbsolutePath());
            }
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Security exception when creating directory: " + dir.getAbsolutePath(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error when creating directory: " + dir.getAbsolutePath(), e);
        }
    }
}
