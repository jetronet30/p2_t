package com.jaba.p2_t.networck;

import com.jaba.p2_t.models.NetModels;
import jakarta.annotation.PostConstruct;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class NetService {

    private static final Logger log = LoggerFactory.getLogger(NetService.class);
    private static final File NETWORCK_FOLDER = new File("/etc/netplan");

    public Map<String, String> maplan() {
    Map<String, String> map = new LinkedHashMap<>();
    for (NetModels model : getNetModels()) {
        map.put(model.getNickname(), model.getIpAddress()); // ან .getName()
    }
    return map;
}

    @SuppressWarnings("unchecked")
    @PostConstruct
    public List<NetModels> getNetModels() {
        List<NetModels> result = new ArrayList<>();

        if (!NETWORCK_FOLDER.exists() || !NETWORCK_FOLDER.isDirectory()) {
            log.warn("Network folder not found: {}", NETWORCK_FOLDER.getAbsolutePath());
            return result;
        }

        File[] yamlFiles = NETWORCK_FOLDER.listFiles((dir, name) -> name.endsWith(".yaml"));
        if (yamlFiles == null) {
            log.warn("No YAML files found in {}", NETWORCK_FOLDER.getAbsolutePath());
            return result;
        }

        Yaml yamlParser = new Yaml();
        int lanIndex = 1;

        for (File yamlFile : yamlFiles) {
            try (FileInputStream fis = new FileInputStream(yamlFile)) {

                Map<String, Object> yamlData = yamlParser.load(fis);
                if (yamlData == null || !(yamlData.get("network") instanceof Map))
                    continue;

                Map<String, Object> network = (Map<String, Object>) yamlData.get("network");
                Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
                if (ethernets == null)
                    continue;

                for (Map.Entry<String, Object> entry : ethernets.entrySet()) {
                    String ifaceName = entry.getKey();
                    if (!(entry.getValue() instanceof Map ifaceConfig))
                        continue;

                    // IP და Subnet
                    List<String> addresses = (List<String>) ifaceConfig.get("addresses");
                    String ipWithCidr = (addresses != null && !addresses.isEmpty()) ? addresses.get(0) : null;

                    String ip = null;
                    String subnet = null;

                    if (ipWithCidr != null && ipWithCidr.contains("/")) {
                        try {
                            SubnetUtils utils = new SubnetUtils(ipWithCidr);
                            utils.setInclusiveHostCount(true);
                            ip = utils.getInfo().getAddress();
                            subnet = utils.getInfo().getNetmask();
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid CIDR in {}: {}", yamlFile.getName(), ipWithCidr, e);
                            continue;
                        }
                    } else {
                        ip = ipWithCidr;
                    }

                    // Gateway fallback: gateway4 or via from default route
                    String gateway = null;
                    if (ifaceConfig.containsKey("gateway4")) {
                        gateway = (String) ifaceConfig.get("gateway4");
                    } else {
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) ifaceConfig.get("routes");
                        if (routes != null) {
                            for (Map<String, Object> route : routes) {
                                Object toVal = route.get("to");
                                if (toVal != null &&
                                        ("default".equalsIgnoreCase(toVal.toString()) ||
                                         "0.0.0.0/0".equals(toVal.toString()))) {
                                    gateway = (String) route.get("via");
                                    break;
                                }
                            }
                        }
                    }

                    // DNS
                    String dns1 = null, dns2 = null;
                    Map<String, Object> nameservers = (Map<String, Object>) ifaceConfig.getOrDefault("nameservers",
                            network.get("nameservers"));
                    if (nameservers != null) {
                        List<String> dnsList = (List<String>) nameservers.get("addresses");
                        if (dnsList != null && !dnsList.isEmpty()) {
                            dns1 = dnsList.get(0);
                            if (dnsList.size() > 1)
                                dns2 = dnsList.get(1);
                        }
                    }

                    // Metric from default route
                    String metric = "0";
                    List<Map<String, Object>> routes = (List<Map<String, Object>>) ifaceConfig.get("routes");
                    if (routes != null) {
                        for (Map<String, Object> route : routes) {
                            Object toVal = route.get("to");
                            if (toVal != null &&
                                    ("default".equalsIgnoreCase(toVal.toString()) ||
                                     "0.0.0.0/0".equals(toVal.toString())) &&
                                    route.get("metric") != null) {
                                metric = String.valueOf(route.get("metric"));
                                break;
                            }
                        }
                    }

                    boolean status = isInterfaceActive(ifaceName);
                    String nickname = "LAN" + lanIndex++;

                    NetModels model = new NetModels(
                            ifaceName,
                            ip,
                            gateway,
                            dns1,
                            dns2,
                            subnet,
                            status,
                            yamlFile.getName(),
                            nickname,
                            metric);

                    result.add(model);
                }

            } catch (IOException | ClassCastException e) {
                log.error("Error reading YAML file: {}", yamlFile.getName(), e);
            }
        }

        return result;
    }

    public String getIpByNickName(String nickname) {
        return getNetModels().stream()
                .filter(m -> nickname.equals(m.getNickname()))
                .map(NetModels::getIpAddress)
                .findFirst()
                .orElse(null);
    }

    public String getNameByNickName(String nickname) {
        return getNetModels().stream()
                .filter(m -> nickname.equals(m.getNickname()))
                .map(NetModels::getName)
                .findFirst()
                .orElse(null);
    }

    private boolean isInterfaceActive(String ifaceName) {
        File carrierFile = new File("/sys/class/net/" + ifaceName + "/carrier");
        try {
            if (carrierFile.exists()) {
                String content = Files.readString(carrierFile.toPath()).trim();
                return "1".equals(content);
            }
        } catch (IOException e) {
            log.error("Failed to read interface status for {}", ifaceName, e);
        }
        return false;
    }
}
