package com.jaba.p2_t.asteriskmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Service
public class AsteriskManager {

    private void executeCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[ASTERISK] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Asterisk command failed with exit code: {}", exitCode);
            } else {
                log.info("Asterisk command executed successfully.");
            }

        } catch (Exception e) {
            log.error("Error executing Asterisk command", e);
        }
    }

    /** Restart full Asterisk service (system level) */
    public void restartAsteriskService() {
        executeCommand("systemctl", "restart", "asterisk");
    }

    /** Reload only the dialplan (extensions.conf or extensions from DB) */
    public void reloadDialplan() {
        executeCommand("asterisk", "-rx", "dialplan reload");
    }

    /** Reload PJSIP configuration from database (if realtime is used) */
    public void reloadPJSIP() {
        executeCommand("asterisk", "-rx", "pjsip reload");
    }

    /** Reload all Asterisk configs */
    public void reloadAll() {
        executeCommand("asterisk", "-rx", "core reload");
    }

    /** Stop Asterisk service */
    public void stopAsteriskService() {
        executeCommand("systemctl", "stop", "asterisk");
    }

    /** Start Asterisk service */
    public void startAsteriskService() {
        executeCommand("systemctl", "start", "asterisk");
    }

    /** Show all PJSIP endpoints and their status */
    public void showEndpoints() {
        executeCommand("asterisk", "-rx", "pjsip show endpoints");
    }

    /** Show PJSIP contacts (used to determine device IPs and registration status) */
    public void showContacts() {
        executeCommand("asterisk", "-rx", "pjsip show contacts");
    }

    /** Show active SIP registrations */
    public void showRegistrations() {
        executeCommand("asterisk", "-rx", "pjsip show registrations");
    }

    /** Show SIP subscriptions */
    public void showSubscriptions() {
        executeCommand("asterisk", "-rx", "pjsip show subscriptions");
    }
}
