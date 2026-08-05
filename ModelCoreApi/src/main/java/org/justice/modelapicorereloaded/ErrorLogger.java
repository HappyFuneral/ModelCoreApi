package org.justice.modelapicorereloaded;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorLogger {

    private final File errorDir;

    public ErrorLogger(File pluginFolder) {
        // Plugin mappán belül létrehozzuk az errors mappát
        this.errorDir = new File(pluginFolder, "errors");
        if (!errorDir.exists()) {
            errorDir.mkdirs();
        }
    }

    public void logError(String category, Exception ex) {
        try {
            // Fájl név: YYYY-MM-dd_HH-mm-ss.log
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            File logFile = new File(errorDir, timestamp + ".log");

            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                pw.println("=== " + category + " ===");
                pw.println("Timestamp: " + LocalDateTime.now());
                pw.println("Exception Message: " + ex.getMessage());
                pw.println("Stack Trace:");
                ex.printStackTrace(pw);
                pw.println();
            }
        } catch (Exception e) {
            // Ha a logolás hibába ütközik, azt itt már nem tudjuk naplózni, esetleg console-ra írd ki
            System.err.println("ErrorLogger hiba: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
