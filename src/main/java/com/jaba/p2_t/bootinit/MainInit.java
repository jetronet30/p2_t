package com.jaba.p2_t.bootinit;

import com.jaba.p2_t.servermanager.ServerSettings;

public class MainInit {

    public static void mainInit(){
        RepoInit.repoCreator();
        ServerSettings.initServerSettings();
        PostgresBootstrapper.init();
    }
}
