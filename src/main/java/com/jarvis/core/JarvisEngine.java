package com.jarvis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JarvisEngine {

    private final List<Command> commands = new ArrayList<>();
    private final Random random = new Random();

    private static final String[] GREETING_RESPONSES = {
        "At your service, sir.",
        "Good day. How may I assist you?",
        "Hello. All systems are operational.",
        "Jarvis online. What do you need?",
        "Standing by for your command."
    };

    private static final String[] UNKNOWN_RESPONSES = {
        "I'm not sure I understand, sir. Could you rephrase that?",
        "That command is not in my repertoire. Type 'help' to see what I can do.",
        "I don't have a protocol for that. Try 'help' for available commands.",
        "Apologies, sir. I didn't catch that. Use 'help' for guidance.",
        "My systems don't recognize that input. Perhaps try another approach?"
    };

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "I'm listening, sir.";
        }

        String trimmed = input.trim().toLowerCase();

        if (isGreeting(trimmed)) {
            return GREETING_RESPONSES[random.nextInt(GREETING_RESPONSES.length)];
        }

        if (trimmed.equals("help") || trimmed.equals("commands")) {
            return buildHelpText();
        }

        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (trimmed.startsWith(trigger.toLowerCase())) {
                    return command.execute(input.trim());
                }
            }
        }

        return UNKNOWN_RESPONSES[random.nextInt(UNKNOWN_RESPONSES.length)];
    }

    private boolean isGreeting(String input) {
        String[] greetings = {"hello", "hi", "hey", "greetings", "good morning",
                              "good afternoon", "good evening", "yo", "sup"};
        for (String g : greetings) {
            if (input.equals(g) || input.startsWith(g + " jarvis") || input.startsWith(g + ",")) {
                return true;
            }
        }
        return false;
    }

    private String buildHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available commands, sir:\n\n");
        for (Command command : commands) {
            sb.append("  ").append(command.getName())
              .append(" — ").append(command.getDescription()).append("\n");
        }
        sb.append("\nYou can also just speak naturally. I'll do my best to understand.");
        return sb.toString();
    }
}
