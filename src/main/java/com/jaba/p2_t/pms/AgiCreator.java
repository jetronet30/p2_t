package com.jaba.p2_t.pms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.springframework.stereotype.Service;
import com.jaba.p2_t.servermanager.ServerSettings;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgiCreator {
    private final ServerSettings serverSettings;

    private static final File AGI_FOLDER = new File("/var/lib/asterisk/agi-bin");
    
    @PostConstruct
    public void createHttpSender( ) throws IOException {
        if (!AGI_FOLDER.exists()) {
            if (!AGI_FOLDER.mkdirs()) {
                throw new IOException("Cannot create AGI folder: " + AGI_FOLDER.getAbsolutePath());
            }
        }

        File agiFile = new File(AGI_FOLDER, "pms_update_http.agi");

        // ვაგენერირებთ სკრიპტს დინამიკურად
        String script = String.format("""
                #!/bin/bash
                # AGI სკრიპტი HTTP POST-ით (ორი სერვერზე გაგზავნა)

                ROOM=$1
                STATUS=$2
                CALLERID=$3

                # ლოკალური PMS
                LOCAL_URL="https://127.0.0.1:%d/roomstatus?room=${ROOM}&status=${STATUS}&src=${CALLERID}"

                # დაშორებული PMS
                # REMOTE_URL="http://%s:%s/pms/roomstatus?room=${ROOM}&status=${STATUS}&src=${CALLERID}"

                # გაგზავნა ორივე სერვერზე
                curl -k -X POST "$LOCAL_URL"
                # curl -s -X POST "$REMOTE_URL"

                echo "SET VARIABLE AGI_RESULT SUCCESS"
                """, serverSettings.getPort(), "192.168.23.23", 8080);

        try (FileWriter writer = new FileWriter(agiFile)) {
            writer.write(script);
        }

        if (!agiFile.setExecutable(true)) {
            throw new IOException("Cannot make AGI script executable: " + agiFile.getAbsolutePath());
        }

        System.out.println("HTTP AGI script created: " + agiFile.getAbsolutePath());
    }

}
