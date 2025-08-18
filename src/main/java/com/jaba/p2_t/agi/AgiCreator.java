package com.jaba.p2_t.agi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AgiCreator {

    private static final File AGI_FOLDER = new File("/var/lib/asterisk/agi-bin");

    @PostConstruct
    public void createHttpSender() throws IOException {
        if (!AGI_FOLDER.exists()) {
            if (!AGI_FOLDER.mkdirs()) {
                throw new IOException("Cannot create AGI folder: " + AGI_FOLDER.getAbsolutePath());
            }
        }

        File agiFile = new File(AGI_FOLDER, "pms_update_http.agi");

        String script = """
                #!/bin/bash
                # AGI სკრიპტი HTTP POST-ით (ავტორიზაციის გარეშე)

                ROOM=$1
                STATUS=$2
                CALLERID=$3

                URL="http://127.0.0.1:8090/pms/roomstatus?room=${ROOM}&status=${STATUS}&src=${CALLERID}"

                curl -s -X POST "$URL"

                echo "SET VARIABLE AGI_RESULT SUCCESS"
                """;

        try (FileWriter writer = new FileWriter(agiFile)) {
            writer.write(script);
        }

        if (!agiFile.setExecutable(true)) {
            throw new IOException("Cannot make AGI script executable: " + agiFile.getAbsolutePath());
        }

        System.out.println("HTTP AGI script created: " + agiFile.getAbsolutePath());
    }

   
    public void createUdpSender(String pmsHost, int pmsPort) throws IOException {
        if (!AGI_FOLDER.exists()) {
            if (!AGI_FOLDER.mkdirs()) {
                throw new IOException("Cannot create AGI folder: " + AGI_FOLDER.getAbsolutePath());
            }
        }

        File agiFile = new File(AGI_FOLDER, "pms_update_udp.agi");

        String script = """
                #!/bin/bash
                # AGI სკრიპტი UDP-ით PMS-სთვის

                ROOM=$1
                STATUS=$2
                CALLERID=$3

                MESSAGE="room=${ROOM}&status=${STATUS}&src=${CALLERID}"

                # გამოაგზავნე UDP packet
                echo -n "$MESSAGE" | nc -u -w1 %s %d

                echo "SET VARIABLE AGI_RESULT SUCCESS"
                """.formatted(pmsHost, pmsPort);

        try (FileWriter writer = new FileWriter(agiFile)) {
            writer.write(script);
        }

        if (!agiFile.setExecutable(true)) {
            throw new IOException("Cannot make AGI script executable: " + agiFile.getAbsolutePath());
        }

        System.out.println("UDP AGI script created: " + agiFile.getAbsolutePath());
    }
}
