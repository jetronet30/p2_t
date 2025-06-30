package com.jaba.p2_t.networck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class NetplanGeneratorDefault {

    private static final Logger log = LoggerFactory.getLogger(NetplanGeneratorDefault.class);

    @SuppressWarnings("unchecked")
    public static boolean generateMinimalNetplanYaml() {
        File targetYamlFile = new File("/etc/netplan/50-cloud-init.yaml");
        File etcNetplanDir = new File("/etc/netplan");

        // Check existing configs
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
                            if (ethernets != null && !ethernets.isEmpty()) {
                                log.info("Existing netplan config detected: {}", file.getName());
                                return false;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing YAML '{}': {}", file.getName(), e.getMessage());
                    }
                }
            }
        }

        // Enumerate usable interfaces
        String[] interfaces = new File("/sys/class/net").list((dir, name) ->
                !name.equals("lo") && (name.startsWith("en") || name.startsWith("eth") || name.startsWith("wl")));

        if (interfaces == null || interfaces.length == 0) {
            log.error("No valid interfaces found to generate netplan.");
            return false;
        }

        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> network = new LinkedHashMap<>();
        Map<String, Object> ethernets = new LinkedHashMap<>();

        int ipCounter = 100;

        for (String iface : interfaces) {
            Map<String, Object> ifaceConfig = new LinkedHashMap<>();
            ifaceConfig.put("dhcp4", false);
            ifaceConfig.put("addresses", List.of("192.168.1." + ipCounter + "/24"));

            Map<String, Object> nameservers = new LinkedHashMap<>();
            nameservers.put("addresses", List.of("8.8.8.8", "8.8.4.4"));
            ifaceConfig.put("nameservers", nameservers);

            Map<String, Object> route = new LinkedHashMap<>();
            route.put("to", "default");
            route.put("via", "192.168.1.1");
            route.put("metric", 0);
            ifaceConfig.put("routes", List.of(route));

            ethernets.put(iface, ifaceConfig);
            ipCounter++;
        }

        network.put("version", 2);
        network.put("renderer", "networkd");
        network.put("ethernets", ethernets);
        root.put("network", network);

        // Write YAML
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try {
            Files.createDirectories(targetYamlFile.toPath().getParent());
            try (FileWriter fw = new FileWriter(targetYamlFile)) {
                yaml.dump(root, fw);
            }
            log.info("Generated default netplan: {}", targetYamlFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            log.error("Failed to write YAML: {}", e.getMessage(), e);
            return false;
        }
    }
}
