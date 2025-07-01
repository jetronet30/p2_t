package com.jaba.p2_t.bootinit;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
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

            createUserAndDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isPostgresInstalled() {
        return runCheck("psql --version");
    }

    private static void createUserAndDatabase() {
        String sql = """
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT FROM pg_catalog.pg_roles WHERE rolname = 'jetronet'
                ) THEN
                    CREATE USER jetronet WITH PASSWORD 'bostana30';
                    ALTER USER jetronet WITH SUPERUSER;
                END IF;
            END
            $$;

            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT FROM pg_database WHERE datname = 'p2_t_db'
                ) THEN
                    CREATE DATABASE p2_t_db OWNER jetronet;
                END IF;
            END
            $$;
        """;

        try {
            Path tmpFile = Files.createTempFile("pginit_", ".sql");
            Files.writeString(tmpFile, sql);
            run("sudo -u postgres psql -f " + tmpFile.toAbsolutePath());
            Files.deleteIfExists(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException("❌ ვერ შეიქმნა დროებითი SQL ფაილი", e);
        }
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
            pb.inheritIO(); // პირდაპირ აჩვენებს კონსოლში stdout/stderr
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("❌ ბრძანების გაშვების შეცდომა: " + command, e);
        }
    }

    


}
