package com.jaba.p2_t.networck;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class NetplanGeneratorDefault {

    private static final Logger log = LoggerFactory.getLogger(NetplanGeneratorDefault.class);

    /**
     * Generates a default netplan YAML at /etc/netplan/50-cloud-init.yaml,
     * if none exists or no usable IPs are found.
     *
     * @return true if new file generated, false otherwise.
     */
    @SuppressWarnings("unchecked")
    public static boolean generateMinimalNetplanYaml() {
        File targetYamlFile = new File("/etc/netplan/50-cloud-init.yaml");
        File etcNetplanDir = new File("/etc/netplan");

        if (etcNetplanDir.exists() && etcNetplanDir.isDirectory()) {
            File[] yamlFiles = etcNetplanDir.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
            if (yamlFiles != null) {
                for (File file : yamlFiles) {
                    try (InputStream is = new FileInputStream(file)) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> data = yaml.load(is);
                        if (data != null && data.containsKey("network")) {
                            Map<String, Object> network = (Map<String, Object>) data.get("network");
                            Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
                            if (ethernets != null) {
                                for (Object ifaceObj : ethernets.values()) {
                                    Map<String, Object> ifaceMap = (Map<String, Object>) ifaceObj;
                                    if (ifaceMap.containsKey("addresses")) {
                                        List<String> addresses = (List<String>) ifaceMap.get("addresses");
                                        if (!addresses.isEmpty()) {
                                            log.info("Valid IP found in netplan config: {}", file.getName());
                                            return false; // Already configured
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing netplan YAML file '{}': {}", file.getName(), e.getMessage());
                    }
                }
            }
        }

        // No valid config found — generate new
        File netDir = new File("/sys/class/net");
        String[] interfaces = netDir.list((dir, name) ->
                !name.equals("lo") &&
                (name.startsWith("en") || name.startsWith("eth") || name.startsWith("wl")));

        if (interfaces == null || interfaces.length == 0) {
            log.error("No valid network interfaces found to generate netplan config.");
            return false;
        }

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> network = new LinkedHashMap<>();
        Map<String, Object> ethernets = new LinkedHashMap<>();

        int ipOffset = 100;
        int metric = 100;

        for (String iface : interfaces) {
            Map<String, Object> ifaceConfig = new LinkedHashMap<>();
            ifaceConfig.put("dhcp4", false);
            ifaceConfig.put("addresses", List.of("192.168.1." + ipOffset + "/24"));
            ifaceConfig.put("gateway4", "192.168.1.1");

            Map<String, Object> nameservers = new LinkedHashMap<>();
            nameservers.put("addresses", List.of("8.8.8.8", "8.8.4.4"));
            ifaceConfig.put("nameservers", nameservers);

            Map<String, Object> route = new LinkedHashMap<>();
            route.put("to", "0.0.0.0/0");
            route.put("via", "192.168.1.1");
            route.put("metric", metric);
            ifaceConfig.put("routes", List.of(route));

            ethernets.put(iface, ifaceConfig);
            ipOffset++;
            metric += 100;
        }

        network.put("version", 2);
        network.put("renderer", "networkd");
        network.put("ethernets", ethernets);
        root.put("network", network);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try {
            Files.createDirectories(targetYamlFile.toPath().getParent());
            try (FileWriter fw = new FileWriter(targetYamlFile)) {
                yaml.dump(root, fw);
            }
            log.info("Generated new netplan YAML: {}", targetYamlFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Failed to write netplan config file '{}': {}", targetYamlFile.getAbsolutePath(), e.getMessage(), e);
            return false;
        }
    }
}
