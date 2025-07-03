package com.jaba.p2_t.servermanager;

import java.io.File;
import java.io.IOException;
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
        private String sipDefPassword;
        private String licenzi;
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

    public static void initServerSettings() {
        if (!SETTINGS.exists()) {
            LOGGER.info("Settings file not found. Creating default settings...");
            config = getDefaultConfig();
            writeToFile();
        } else {
            readFromFile();
        }
    }

    public void editSetting(int _port, int _dataPort, String _dataUser, String _dataPassword,
            String _dataName, String _dataHost, String _licenzi) {
        config.setPort(_port);
        config.setDataPort(_dataPort);
        config.setDataUser(_dataUser);
        config.setDataPassword(_dataPassword);
        config.setDataName(_dataName);
        config.setDataHost(_dataHost);
        config.setLicenzi(_licenzi);
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
        def.setSipDefPassword("minitelsy2");
        return def;
    }

    public static synchronized ServerConfig getConfig() {
        return config;
    }
}
