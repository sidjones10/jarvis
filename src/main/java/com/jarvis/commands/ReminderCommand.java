package com.jarvis.commands;

import com.jarvis.core.Command;
import com.jarvis.core.ResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderCommand implements Command {

    private final List<String> reminders = new ArrayList<>();
    private ResponseListener listener;

    public void setResponseListener(ResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public String getName() {
        return "remind <minutes> <message>";
    }

    @Override
    public String getDescription() {
        return "Sets a timed reminder (e.g., remind 5 Take a break).";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"remind ", "reminder ", "set reminder"};
    }

    @Override
    public String execute(String input) {
        String cleaned = input.replaceFirst("(?i)(remind|reminder|set reminder)\\s+", "").trim();
        String[] parts = cleaned.split("\\s+", 2);

        if (parts.length < 2) {
            return "Usage: remind <minutes> <message>\nExample: remind 5 Take a break";
        }

        try {
            int minutes = Integer.parseInt(parts[0]);
            String message = parts[1];
            reminders.add(message);

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String alert = "⏰ REMINDER: " + message;
                    if (listener != null) {
                        listener.onResponse(alert);
                    }
                    reminders.remove(message);
                }
            }, minutes * 60L * 1000L);

            return String.format("Reminder set, sir. I'll notify you in %d minute%s: \"%s\"",
                    minutes, minutes == 1 ? "" : "s", message);
        } catch (NumberFormatException e) {
            return "Please specify the time in minutes. Example: remind 5 Check the build";
        }
    }
}
