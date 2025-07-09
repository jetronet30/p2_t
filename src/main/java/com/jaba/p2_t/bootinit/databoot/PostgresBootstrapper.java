package com.jaba.p2_t.bootinit.databoot;

import org.springframework.stereotype.Service;

import com.jaba.p2_t.servermanager.ServerSettings;

import lombok.RequiredArgsConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class PostgresBootstrapper {

    public static void init() {
        try {
            if (!isPostgresInstalled()) {
                System.out.println("🔧 Installing PostgreSQL...");
                run("apt update");
                run("apt install -y postgresql postgresql-contrib");
                run("systemctl start postgresql");
            } else {
                System.out.println("✅ PostgreSQL already installed.");
            }

            initWithPsqlScript();
            donloadAndInstallODBC();
            genrateODBC();

        } catch (Exception e) {
            System.err.println("❌ Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isPostgresInstalled() {
        return runCheck("psql --version");
    }

    private static boolean runCheck(String command) {
        try {
            Process p = new ProcessBuilder("bash", "-c", command).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void run(String command) {
        try {
            System.out.println("▶️ " + command);
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("❌ Command failed: " + command, e);
        }
    }

    private static void initWithPsqlScript() {
        String dbUser = System.getenv().getOrDefault("JETRONET_USER", ServerSettings.s_getDataUser());
        String dbPass = System.getenv().getOrDefault("JETRONET_PASS", ServerSettings.s_getDataPassword());
        String dbName = System.getenv().getOrDefault("JETRONET_DB", ServerSettings.s_getDataName());

        File tempFile = null;
        try {
            String sql = String.format("""
                    DO $$
                    BEGIN
                        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '%s') THEN
                            CREATE USER %s WITH PASSWORD '%s';
                        END IF;
                    END
                    $$;

                    DO $$
                    BEGIN
                        IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '%s') THEN
                            ALTER USER %s WITH SUPERUSER;
                        END IF;
                    END
                    $$;

                    SELECT 'CREATE DATABASE %s OWNER %s'
                    WHERE NOT EXISTS (
                        SELECT FROM pg_database WHERE datname = '%s'
                    )\\gexec
                    """, dbUser, dbUser, dbPass, dbUser, dbUser, dbName, dbUser, dbName);

            tempFile = File.createTempFile("initdb-", ".sql");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
                bw.write(sql);
            }

            String command = "sudo -u postgres psql -f " + tempFile.getAbsolutePath();
            System.out.println("▶️ Executing SQL script...");
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ PostgreSQL setup complete.");
            } else {
                System.err.println("❌ SQL script failed with code: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Error executing SQL script: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (Exception ignore) {
                }
            }
        }
    }

    private static void genrateODBC() {
        try {
            // extconfig.conf
            String extconfigContent = """
                    [settings]
                    ps_endpoints => odbc,asterisk,ps_endpoints
                    ps_auths => odbc,asterisk,ps_auths
                    ps_aors => odbc,asterisk,ps_aors
                    """;
            writeFile("/etc/asterisk/extconfig.conf", extconfigContent);

            // sorcery.conf
            String sorceryContent = """
                    [res_pjsip]
                    endpoint=realtime,ps_endpoints
                    auth=realtime,ps_auths
                    aor=realtime,ps_aors
                    """;
                    
            writeFile("/etc/asterisk/sorcery.conf", sorceryContent);

            // odbc.ini
            String odbcIniContent = """
                    [asterisk]
                    Description=Asterisk DB Connection
                    Driver=PostgreSQL
                    Database=%s
                    Servername=%s
                    Port=%d
                    UserName=%s
                    Password=%s
                    """.formatted(
                    ServerSettings.s_getDataName(),
                    ServerSettings.s_getDataHost(),
                    ServerSettings.s_getDataPort(),
                    ServerSettings.s_getDataUser(),
                    ServerSettings.s_getDataPassword());
            writeFile("/etc/odbc.ini", odbcIniContent);

            // odbcinst.ini
            String odbcInstContent = """
                    [PostgreSQL]
                    Description=ODBC for PostgreSQL
                    Driver=/usr/lib/x86_64-linux-gnu/odbc/psqlodbcw.so
                    Setup=/usr/lib/x86_64-linux-gnu/odbc/libodbcpsqlS.so
                    FileUsage=1
                    """;
            writeFile("/etc/odbcinst.ini", odbcInstContent);

            System.out.println("✅ ODBC configuration files written successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to generate ODBC config files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void donloadAndInstallODBC() {
        try {
            // შემოწმება: დაყენებულია თუ არა odbc-postgresql
            Process checkProcess = new ProcessBuilder("bash", "-c", "dpkg -s odbc-postgresql").start();
            int checkCode = checkProcess.waitFor();

            if (checkCode == 0) {
                System.out.println("✅ odbc-postgresql already installed.");
                return;
            }

            System.out.println("🔧 Installing odbc-postgresql...");

            // apt-get update && install
            new ProcessBuilder("bash", "-c", "apt update && apt install -y odbc-postgresql")
                    .inheritIO()
                    .start()
                    .waitFor();

            System.out.println("✅ odbc-postgresql installed successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to install odbc-postgresql: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeFile(String path, String content) throws Exception {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

}
