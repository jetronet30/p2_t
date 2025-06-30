package com.jaba.p2_t.networck;

import com.jaba.p2_t.models.NetModels;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LanConfigWritter {

    private final NetService netService;

    @SuppressWarnings("unchecked")
    public void setLan(String nickname, String ip, String gateway, String dns1, String dns2, String subnet) {
        List<NetModels> configs = netService.getNetModels();

        NetModels match = configs.stream()
                .filter(m -> nickname.equals(m.getNickname()))
                .findFirst()
                .orElse(null);

        if (match == null) {
            System.out.println("Nickname not found: " + nickname);
            return;
        }

        String iface = match.getName();
        String yamlFileName = match.getYamlFileName();
        File yamlFile = new File("./maintestrepo/" + yamlFileName); // "/etc/netplan/" რეალურ გამოყენებაში

        try (FileInputStream fis = new FileInputStream(yamlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            if (data == null) data = new LinkedHashMap<>();

            Map<String, Object> network = (Map<String, Object>) data.get("network");
            if (network == null) {
                network = new LinkedHashMap<>();
                data.put("network", network);
            }

            network.put("version", 2);
            network.put("renderer", "networkd");

            Map<String, Object> ethernets = (Map<String, Object>) network.get("ethernets");
            if (ethernets == null) {
                ethernets = new LinkedHashMap<>();
                network.put("ethernets", ethernets);
            }

            Map<String, Object> ifaceConfig = new LinkedHashMap<>();
            ifaceConfig.put("dhcp4", false);

            // Subnet შეფასება: თუ "8", "16", "24" პირდაპირ გამოიყენე, თუ არა - გარდაქმნა
            String cidrSuffix;
            if (subnet.equals("8") || subnet.equals("16") || subnet.equals("24")) {
                cidrSuffix = subnet;
            } else {
                cidrSuffix = String.valueOf(cidrFromSubnet(subnet));
            }

            ifaceConfig.put("addresses", List.of(ip + "/" + cidrSuffix));
            ifaceConfig.put("gateway4", gateway);

            Map<String, Object> nameservers = new LinkedHashMap<>();
            List<String> dnsList = new ArrayList<>();
            if (dns1 != null && !dns1.isBlank()) dnsList.add(dns1);
            if (dns2 != null && !dns2.isBlank()) dnsList.add(dns2);
            nameservers.put("addresses", dnsList);
            ifaceConfig.put("nameservers", nameservers);

            Map<String, Object> route = new LinkedHashMap<>();
            route.put("to", "0.0.0.0/0");
            route.put("via", gateway);
            route.put("metric", getMetricFromNickname(nickname));

            ifaceConfig.put("routes", List.of(route));
            ethernets.put(iface, ifaceConfig);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml outYaml = new Yaml(options);

            try (FileWriter fw = new FileWriter(yamlFile)) {
                outYaml.dump(data, fw);
                System.out.println("Updated: " + yamlFile.getAbsolutePath());
            }

        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
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

    private int getMetricFromNickname(String nickname) {
        if (nickname != null && nickname.startsWith("LAN")) {
            try {
                return Integer.parseInt(nickname.substring(3)) * 100;
            } catch (NumberFormatException ignored) {
            }
        }
        return 100;
    }
}
