package com.jaba.p2_t.networck;

import com.jaba.p2_t.models.NetModels;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LanConfigWritter {

    private static final Logger log = LoggerFactory.getLogger(LanConfigWritter.class);
    private final NetService netService;

    @SuppressWarnings("unchecked")
    public void setLan(String nickname, String ip, String gateway, String dns1, String dns2, String subnet,
                       String metric) {
        if (!isValidIPv4(ip) || !isValidIPv4(gateway)) {
            log.warn("Invalid IP or gateway format: ip={}, gateway={}", ip, gateway);
            return;
        }
        if ((dns1 != null && !dns1.isBlank() && !isValidIPv4(dns1)) ||
                (dns2 != null && !dns2.isBlank() && !isValidIPv4(dns2))) {
            log.warn("Invalid DNS format: dns1={}, dns2={}", dns1, dns2);
            return;
        }
        if (!isValidSubnet(subnet)) {
            log.warn("Invalid subnet format: {}", subnet);
            return;
        }

        List<NetModels> configs = netService.getNetModels();
        for (NetModels model : configs) {
            if (!nickname.equals(model.getNickname()) && ip.equals(model.getIpAddress())) {
                log.error("IP conflict detected: {} already used by {}", ip, model.getNickname());
                return;
            }
        }

        NetModels match = configs.stream()
                .filter(m -> nickname.equals(m.getNickname()))
                .findFirst()
                .orElse(null);

        if (match == null) {
            log.warn("Nickname '{}' not found", nickname);
            return;
        }

        String iface = match.getName();
        String yamlFileName = match.getYamlFileName();
        File yamlFile = new File("/etc/netplan/" + yamlFileName);

        if (!yamlFile.exists()) {
            log.error("YAML file does not exist: {}", yamlFile.getAbsolutePath());
            return;
        }

        try {
            File backup = new File(yamlFile.getAbsolutePath() + ".bak");
            Files.copy(yamlFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Backup created: {}", backup.getName());
        } catch (IOException e) {
            log.error("Failed to create backup for {}", yamlFile.getName(), e);
            return;
        }

        try (FileInputStream fis = new FileInputStream(yamlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            if (data == null) data = new LinkedHashMap<>();

            Map<String, Object> network = (Map<String, Object>) data.getOrDefault("network", new LinkedHashMap<>());
            data.put("network", network);
            network.put("version", 2);

            Map<String, Object> ethernets = (Map<String, Object>) network.getOrDefault("ethernets", new LinkedHashMap<>());
            network.put("ethernets", ethernets);

            Map<String, Object> ifaceConfig = new LinkedHashMap<>();
            ifaceConfig.put("dhcp4", false);

            String cidrSuffix = (subnet.matches("8|16|24")) ? subnet : String.valueOf(cidrFromSubnet(subnet));
            ifaceConfig.put("addresses", List.of(ip + "/" + cidrSuffix));

            Map<String, Object> nameservers = new LinkedHashMap<>();
            List<String> dnsList = new ArrayList<>();
            if (dns1 != null && !dns1.isBlank()) dnsList.add(dns1);
            if (dns2 != null && !dns2.isBlank()) dnsList.add(dns2);
            nameservers.put("addresses", dnsList);
            ifaceConfig.put("nameservers", nameservers);

            String metricToSet = (metric != null && !metric.isBlank()) ? metric : "100";

            Map<String, Object> route = new LinkedHashMap<>();
            route.put("to", "default");
            route.put("via", gateway);
            route.put("metric", Integer.parseInt(metricToSet));
            ifaceConfig.put("routes", List.of(route));

            ethernets.put(iface, ifaceConfig);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml outYaml = new Yaml(options);

            try (FileWriter fw = new FileWriter(yamlFile)) {
                outYaml.dump(data, fw);
                log.info("Updated YAML: {}", yamlFile.getAbsolutePath());
            }

        } catch (IOException | ClassCastException e) {
            log.error("Failed to update LAN config for {}", iface, e);
        }
    }

    private int cidrFromSubnet(String subnetMask) {
        return switch (subnetMask) {
            case "255.0.0.0" -> 8;
            case "255.255.0.0" -> 16;
            case "255.255.255.0" -> 24;
            case "255.255.255.128" -> 25;
            case "255.255.255.192" -> 26;
            case "255.255.255.224" -> 27;
            case "255.255.255.240" -> 28;
            case "255.255.255.248" -> 29;
            case "255.255.255.252" -> 30;
            case "255.255.255.254" -> 31;
            default -> 24;
        };
    }

    private boolean isValidIPv4(String ip) {
        return ip != null && ip.matches("^(25[0-5]|2[0-4]\\d|1?\\d{1,2})(\\.(25[0-5]|2[0-4]\\d|1?\\d{1,2})){3}$");
    }

    private boolean isValidSubnet(String subnet) {
        return subnet != null && (subnet.matches("^(8|16|24|25|26|27|28|29|30|31)$") ||
                subnet.matches("^(255\\.(255|0|128|192|224|240|248|252|254)(\\.\\d{1,3}){2})$"));
    }

    public boolean applyNetplan() {
        try {
            Process process = new ProcessBuilder("netplan", "try").start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("netplan apply succeeded.");
                return true;
            } else {
                log.error("netplan apply failed. Exit code: {}", exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error while applying netplan", e);
            return false;
        }
    }
}
