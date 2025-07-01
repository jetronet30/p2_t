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



        } catch (Exception e) {
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
            pb.inheritIO(); // პირდაპირ აჩვენებს კონსოლში stdout/stderr
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("❌ ბრძანების გაშვების შეცდომა: " + command, e);
        }
    }

    private static void initWithInteractivePsql() {
    try {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "sudo -u postgres psql");
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        // Helper: ელოდება prompt-ს ან კონკრეტულ პასუხს
        String waitFor(BufferedReader r, String keyword) throws IOException {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println("<< " + line); // optional debug
                output.append(line).append("\n");
                if (line.contains(keyword)) break;
            }
            return output.toString();
        }

        // Step 1: დაველოდოთ prompt-ს
        waitFor(reader, "postgres=#");

        // Step 2: შექმნა მომხმარებელი
        writer.write("CREATE USER jetronet WITH PASSWORD 'bostana30';\n");
        writer.flush();
        String out1 = waitFor(reader, "CREATE ROLE");

        // Step 3: SUPERUSER
        writer.write("ALTER USER jetronet WITH SUPERUSER;\n");
        writer.flush();
        String out2 = waitFor(reader, "ALTER ROLE");

        // Step 4: ბაზის შექმნა
        writer.write("CREATE DATABASE p2_t_db OWNER jetronet;\n");
        writer.flush();
        String out3 = waitFor(reader, "CREATE DATABASE");

        // Step 5: გასვლა
        writer.write("\\q\n");
        writer.flush();

        process.waitFor();

    } catch (Exception e) {
        e.printStackTrace();
    }
}



}
