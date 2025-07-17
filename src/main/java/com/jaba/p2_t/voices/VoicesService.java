package com.jaba.p2_t.voices;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.springframework.stereotype.Service;

@Service
public class VoicesService {
    private static final File VOICE_FOLDER = new File("/var/lib/asterisk/sounds/voicemessages");

    public List<String> getVoiceFileNames() {
        if (!VOICE_FOLDER.exists()) {
            VOICE_FOLDER.mkdir();
        }

        List<String> voiceNames = new ArrayList<>();

        if (VOICE_FOLDER.exists() && VOICE_FOLDER.isDirectory()) {
            Collection<File> voiceFiles = FileUtils.listFiles(
                    VOICE_FOLDER,
                    new SuffixFileFilter(new String[]{".wav", ".gsm", ".ulaw"}),
                    null
            );

            for (File file : voiceFiles) {
                String name = file.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex > 0) {
                    name = name.substring(0, dotIndex); 
                }
                voiceNames.add(name);
            }
        }

        return voiceNames;
    }
}
