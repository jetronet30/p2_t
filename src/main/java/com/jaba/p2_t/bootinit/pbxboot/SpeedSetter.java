package com.jaba.p2_t.bootinit.pbxboot;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.models.NetModels;
import com.jaba.p2_t.networck.NetService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SpeedSetter {
    private static final Logger log = LoggerFactory.getLogger(SpeedSetter.class);

    private final NetService netService;

    @PostConstruct
    public void setSpeed() {
        for (NetModels nt : netService.getNetModels()) {
            if (nt.isStatus() && nt.getSpeed() != null && nt.getSpeed().matches("\\d+")) {
                String command = String.format(
                        "ethtool -s %s speed %s duplex full autoneg off",
                        nt.getName(),
                        nt.getSpeed()
                );
                try {
                    Process process = new ProcessBuilder("bash", "-c", command).start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        log.info("Speed set successfully: {} -> {} Mbps (duplex full, autoneg off)", nt.getName(), nt.getSpeed());
                    } else {
                        log.warn("Failed to set speed for {}. Exit code {}", nt.getName(), exitCode);
                    }
                } catch (Exception e) {
                    log.error("Error setting speed for {}", nt.getName(), e);
                }
            } else {
                log.info("Skipping interface {} (status={}, speed={})",
                        nt.getName(), nt.isStatus(), nt.getSpeed());
            }
        }
    }
}
