package com.jarvis.commands;

import com.jarvis.core.Command;

public class SystemInfoCommand implements Command {

    @Override
    public String getName() {
        return "system / status";
    }

    @Override
    public String getDescription() {
        return "Displays system information and diagnostics.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"system", "status", "diagnostics", "sysinfo"};
    }

    @Override
    public String execute(String input) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        int processors = runtime.availableProcessors();
        String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");
        String user = System.getProperty("user.name");

        return String.format(
            "System diagnostics, sir:\n\n" +
            "  OS:             %s\n" +
            "  Java Version:   %s\n" +
            "  User:           %s\n" +
            "  Processors:     %d cores\n" +
            "  Memory Used:    %d MB / %d MB\n" +
            "  Memory Max:     %d MB\n\n" +
            "All systems nominal.",
            os, javaVersion, user, processors, usedMemory, totalMemory, maxMemory
        );
    }
}
