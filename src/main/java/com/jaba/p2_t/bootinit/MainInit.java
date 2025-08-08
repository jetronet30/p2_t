package com.jaba.p2_t.bootinit;

import com.jaba.p2_t.bootinit.databoot.PostgresBootstrapper;
import com.jaba.p2_t.bootinit.ffmpeg.FFmpegManager;
import com.jaba.p2_t.bootinit.pbxboot.Pbxinit;
import com.jaba.p2_t.servermanager.ServerSettings;

public class MainInit {

    public static void mainInit(){
        RepoInit.repoCreator();
        ServerSettings.initServerSettings();
        PostgresBootstrapper.init();
        FFmpegManager.ffmpegInit();
        Pbxinit.writeModules();

    }
}
