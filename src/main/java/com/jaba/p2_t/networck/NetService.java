package com.jaba.p2_t.networck;

import com.jaba.p2_t.models.NetModels;
import jakarta.annotation.PostConstruct;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class NetService {

    private static final File NETWORCK_FOLDER = new File("./maintestrepo/");

    @SuppressWarnings("unchecked")
    @PostConstruct
    public List<NetModels> getNetModels() {
        List<NetModels> result = new ArrayList<>();

        if (!NETWORCK_FOLDER.exists() || !NETWORCK_FOLDER.isDirectory()) {
            return result;
        }

        File[] yamlFiles = NETWORCK_FOLDER.listFiles((dir, name) -> name.endsWith(".yaml"));
        if (yamlFiles == null) return result;

        Yaml yamlParser = new Yaml();
        int lanIndex = 1;

        for (File yamlFile : yamlFiles) {
            try (FileInputStream fis = new FileInputStream(yamlFile)) {

                Map<String, Object> yamlData = yamlParser.load(fis);
                if (yamlData == null) continue;

                Map<String, Object> network = (Map<String, Object>) yamlData.get("network");
                if (network == null) continue;

                Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
                if (ethernets == null) continue;

                for (Map.Entry<String, Object> entry : ethernets.entrySet()) {
                    String ifaceName = entry.getKey();
                    Map<String, Object> ifaceConfig = (Map<String, Object>) entry.getValue();

                    // IP და Subnet
                    List<String> addresses = (List<String>) ifaceConfig.get("addresses");
                    String ipWithCidr = (addresses != null && !addresses.isEmpty()) ? addresses.get(0) : null;

                    String ip = null;
                    String subnet = null;

                    if (ipWithCidr != null && ipWithCidr.contains("/")) {
                        try {
                            SubnetUtils utils = new SubnetUtils(ipWithCidr);
                            subnet = utils.getInfo().getNetmask();
                            ip = utils.getInfo().getAddress(); // ან უბრალოდ ipWithCidr.split("/")[0]
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ip = ipWithCidr;
                    }

                    // Gateway
                    String gateway = (String) network.get("gateway4");
                    if (gateway == null) {
                        gateway = (String) ifaceConfig.get("gateway4");
                    }

                    // DNS
                    String dns1 = null, dns2 = null;
                    Map<String, Object> nameservers = (Map<String, Object>) ifaceConfig.get("nameservers");
                    if (nameservers == null) {
                        nameservers = (Map<String, Object>) network.get("nameservers");
                    }
                    if (nameservers != null) {
                        List<String> dnsList = (List<String>) nameservers.get("addresses");
                        if (dnsList != null && !dnsList.isEmpty()) {
                            dns1 = dnsList.get(0);
                            if (dnsList.size() > 1) {
                                dns2 = dnsList.get(1);
                            }
                        }
                    }

                    // Interface link status (true თუ კაბელი/სიგნალი არის)
                    boolean status = isInterfaceActive(ifaceName);

                    // Nickname
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
                            nickname
                    );

                    result.add(model);
                }

            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private boolean isInterfaceActive(String ifaceName) {
        File carrierFile = new File("/sys/class/net/" + ifaceName + "/carrier");
        try {
            if (carrierFile.exists()) {
                String content = Files.readString(carrierFile.toPath());
                return content.equals("1");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
