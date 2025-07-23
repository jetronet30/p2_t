package com.jaba.p2_t.pbxservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.asteriskmanager.AsteriskManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DialPlanGenerator {
    private final AsteriskManager asteriskManager;
    private static final File EXTENSIONS_CONF = new File("/etc/asterisk/extensions.conf");
    private static final File CALL_GROUP_CONF = new File("/etc/asterisk/custom_callgroup.conf");
    private static final File TRUNKS_CONF = new File("/etc/asterisk/custom_trunks.conf");
    private static final File FORWARDING_CONF = new File("/etc/asterisk/autoforwarding.conf");

    @PostConstruct
    public void createDefaultContext() {
        createFiles();
        generateDialPlan();
    }

    private void createFiles() {
        try {
            if (!CALL_GROUP_CONF.exists())
                CALL_GROUP_CONF.createNewFile();
            if (!TRUNKS_CONF.exists())
                TRUNKS_CONF.createNewFile();
            if (!FORWARDING_CONF.exists())
                FORWARDING_CONF.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateDialPlan() {
        if (!EXTENSIONS_CONF.exists()) {
            try {
                EXTENSIONS_CONF.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EXTENSIONS_CONF, true))) {
                writer.write("\n[general]\n");

                writer.write("#include /etc/asterisk/autoforwarding.conf\n");
                writer.write("#include /etc/asterisk/custom_callgroup.conf\n");
                writer.write("#include /etc/asterisk/custom_trunks.conf\n");
                writer.write("#include /etc/asterisk/outbounds.conf\n");

                writer.write("\n[default]\n");
                writer.write("exten => _X.,1,NoOp(Calling number: ${CALLERID(num)} → ${EXTEN})\n");
                writer.write("same => n,Set(CONTACT=${PJSIP_DIAL_CONTACTS(${EXTEN})})\n");
                writer.write("same => n,GotoIf($[\"${CONTACT}\" = \"\"]?nocon:found)\n");
                writer.write("same => n(nocon),Playback(en/vm-nobodyavail)\n");
                writer.write("same => n,Hangup()\n");
                writer.write("same => n(found),Dial(${CONTACT},60)\n");
                writer.write("same => n,Goto(${DIALSTATUS},1)\n");
                writer.write("exten => BUSY,1,Playback(en/please-try-call-later)\n");
                writer.write("same => n,Hangup()\n");
                writer.write("exten => NOANSWER,1,Playback(en/vm-nobodyavail)\n");
                writer.write("same => n,Hangup()\n");
                writer.write("exten => UNAVAIL,1,Playback(en/extension-not-available)\n");
                writer.write("same => n,Hangup()\n");
                writer.write("exten => CONGESTION,1,Playback(en/congestion)\n");
                writer.write("same => n,Hangup()\n");
                writer.write("exten => _,1,Hangup()\n\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
            asteriskManager.reloadDialplan();

        }

    }

}
