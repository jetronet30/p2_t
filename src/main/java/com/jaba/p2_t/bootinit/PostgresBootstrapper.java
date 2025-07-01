package com.jaba.p2_t.bootinit;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

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

            initWithPsqlScript();

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
        String dbUser = System.getenv().getOrDefault("JETRONET_USER", "jetronet");
        String dbPass = System.getenv().getOrDefault("JETRONET_PASS", "bostana30");
        String dbName = System.getenv().getOrDefault("JETRONET_DB", "p2_t_db");

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
}
