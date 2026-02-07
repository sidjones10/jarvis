package com.jarvis.commands;

import com.jarvis.core.Command;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeCommand implements Command {

    @Override
    public String getName() {
        return "time / date";
    }

    @Override
    public String getDescription() {
        return "Shows the current date and time.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"time", "date", "what time", "what's the time", "what day"};
    }

    @Override
    public String execute(String input) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        return "It is currently " + time + " on " + date + ", sir.";
    }
}
