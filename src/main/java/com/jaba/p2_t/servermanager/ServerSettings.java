package com.jaba.p2_t.servermanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jaba.p2_t.bootinit.RepoInit;

import lombok.Getter;
import lombok.Setter;

@Service
public class ServerSettings {
    private static final Logger LOGGER = Logger.getLogger(ServerSettings.class.getName());
    private static final File SETTINGS = new File(RepoInit.SERVER_RESOURCES, "server_settings.json");
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static ServerConfig config;

    @Getter
    @Setter
    public static class ServerConfig {
        private int port;
        private String dataUser;
        private String dataPassword;
        private String dataName;
        private String dataHost;
        private int dataPort;
        private String licenzi;
        private boolean useData;
        private String timeZone;
    }

    public static String getFullDBHost() {
        if (config == null) {
            initServerSettings(); // თუ ჯერ არ არის დატვირთული
        }
        return "jdbc:postgresql://" + config.getDataHost() + ":" + config.getDataPort() + "/" + config.getDataName();
    }
    public int getPort(){
        return config.getPort();
    }
    public int getDataPort(){
        return config.getDataPort();
    }
    public String getDataUser(){
        return config.getDataUser();
    }
    public String getDataPassword(){
        return config.getDataPassword();
    }
    public String getDataName(){
        return config.getDataName();
    }
    public String getDataHost(){
        return config.getDataHost();
    }
    public String getLicenzi(){
        return config.getLicenzi();
    }

    public String getTimeZone(){
        return config.getTimeZone();
    }

    public static int s_getPort(){
        return config.getPort();
    }

    public static int s_getDataPort(){
        return config.getDataPort();
    }
    public static String s_getDataUser(){
        return config.getDataUser();
    }
    public static String s_getDataPassword(){
        return config.getDataPassword();
    }
    public static String s_getDataName(){
        return config.getDataName();
    }
    public static String s_getDataHost(){
        return config.getDataHost();
    }
    public static String s_getTimeZone(){
        return config.getTimeZone();
    }


    public static void initServerSettings() {
        if (!SETTINGS.exists()) {
            LOGGER.info("Settings file not found. Creating default settings...");
            config = getDefaultConfig();
            writeToFile();
        } else {
            readFromFile();
        }
    }

    public void editSetting(int _port, String _licenzi, int _dataPort, String _dataUser, String _dataPassword,
            String _dataName, String _dataHost, String _timeZone) {
        config.setPort(_port);
        config.setDataPort(_dataPort);
        config.setDataUser(_dataUser);
        config.setDataPassword(_dataPassword);
        config.setDataName(_dataName);
        config.setDataHost(_dataHost);
        config.setLicenzi(_licenzi);
        config.setTimeZone(_timeZone);
        setTimeZoneInSys(_timeZone);
        writeToFile();
    }

    private static void writeToFile() {
        try {
            mapper.writeValue(SETTINGS, config);
            LOGGER.info("Server settings saved to JSON.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write settings to file: " + SETTINGS.getAbsolutePath(), e);
        }
    }

    private static void readFromFile() {
        try {
            config = mapper.readValue(SETTINGS, ServerConfig.class);
            LOGGER.info("Server settings loaded successfully.");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read settings from file. Reverting to defaults.", e);
            config = getDefaultConfig();
            writeToFile();
        }
    }

    private static ServerConfig getDefaultConfig() {
        ServerConfig def = new ServerConfig();
        def.setPort(8090);
        def.setDataPort(5432);
        def.setDataUser("jetronet");
        def.setDataPassword("bostana30");
        def.setDataName("p2_t_db");
        def.setDataHost("localhost");
        def.setLicenzi("demo");
        def.setUseData(true);
        def.setTimeZone("Asia/Tbilisi");
        setTimeZoneInSys(def.getTimeZone());
        return def;
    }

    public static synchronized ServerConfig getConfig() {
        return config;
    }

    private static void setTimeZoneInSys(String timezone) {
        try {
            String[] cmd = { "timedatectl", "set-timezone", timezone };

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Time zone changed to: " + timezone);
            } else {
                System.err.println("Failed to change time zone. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
