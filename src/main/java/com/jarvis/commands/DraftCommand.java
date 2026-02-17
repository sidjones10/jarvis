package com.jarvis.commands;

import com.jarvis.core.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DraftCommand implements Command {

    private final Path draftsDir;

    public DraftCommand() {
        this.draftsDir = Paths.get(System.getProperty("user.home"), ".jarvis", "drafts");
        try {
            Files.createDirectories(draftsDir);
        } catch (IOException e) {
            // Directory creation failed; operations will report errors individually
        }
    }

    @Override
    public String getName() {
        return "draft <save|list|show|delete> [name] [content]";
    }

    @Override
    public String getDescription() {
        return "Save, list, show, or delete text drafts.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"draft ", "drafts"};
    }

    @Override
    public String execute(String input) {
        String cleaned = input.replaceFirst("(?i)drafts?\\s*", "").trim();

        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("list")) {
            return listDrafts();
        }

        String[] parts = cleaned.split("\\s+", 3);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "save":
                return handleSave(parts);
            case "show":
            case "view":
                return handleShow(parts);
            case "delete":
            case "remove":
                return handleDelete(parts);
            case "list":
                return listDrafts();
            default:
                // Treat as "draft save <name> <content>" shorthand: "draft <name> <content>"
                String name = parts[0];
                String content = cleaned.substring(name.length()).trim();
                if (content.isEmpty()) {
                    return "Usage:\n"
                         + "  draft save <name> <content>  — Save a draft\n"
                         + "  draft list                   — List all drafts\n"
                         + "  draft show <name>            — Show a draft\n"
                         + "  draft delete <name>          — Delete a draft";
                }
                return saveDraft(name, content);
        }
    }

    private String handleSave(String[] parts) {
        if (parts.length < 3) {
            return "Usage: draft save <name> <content>\nExample: draft save meeting-notes Discuss Q3 roadmap";
        }
        String name = parts[1];
        String content = parts[2];
        return saveDraft(name, content);
    }

    private String handleShow(String[] parts) {
        if (parts.length < 2) {
            return "Usage: draft show <name>";
        }
        String name = sanitizeName(parts[1]);
        Path file = draftsDir.resolve(name + ".txt");
        if (!Files.exists(file)) {
            return "No draft found with name \"" + name + "\". Use 'draft list' to see saved drafts.";
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            return "Draft \"" + name + "\":\n\n" + content;
        } catch (IOException e) {
            return "Error reading draft: " + e.getMessage();
        }
    }

    private String handleDelete(String[] parts) {
        if (parts.length < 2) {
            return "Usage: draft delete <name>";
        }
        String name = sanitizeName(parts[1]);
        Path file = draftsDir.resolve(name + ".txt");
        if (!Files.exists(file)) {
            return "No draft found with name \"" + name + "\".";
        }
        try {
            Files.delete(file);
            return "Draft \"" + name + "\" deleted, sir.";
        } catch (IOException e) {
            return "Error deleting draft: " + e.getMessage();
        }
    }

    private String saveDraft(String name, String content) {
        name = sanitizeName(name);
        Path file = draftsDir.resolve(name + ".txt");
        try {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "Draft \"" + name + "\" saved, sir.";
        } catch (IOException e) {
            return "Error saving draft: " + e.getMessage();
        }
    }

    private String listDrafts() {
        try (Stream<Path> files = Files.list(draftsDir)) {
            java.util.List<String> names = files
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> {
                        String fname = p.getFileName().toString();
                        return fname.substring(0, fname.length() - 4);
                    })
                    .sorted()
                    .collect(Collectors.toList());

            if (names.isEmpty()) {
                return "No drafts saved yet, sir. Use 'draft save <name> <content>' to create one.";
            }

            StringBuilder sb = new StringBuilder("Saved drafts:\n");
            for (String name : names) {
                sb.append("  - ").append(name).append("\n");
            }
            sb.append("\nUse 'draft show <name>' to view a draft.");
            return sb.toString();
        } catch (IOException e) {
            return "Error listing drafts: " + e.getMessage();
        }
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
