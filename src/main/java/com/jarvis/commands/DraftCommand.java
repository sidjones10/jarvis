package com.jarvis.commands;

import com.jarvis.core.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
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

    public Path getDraftsDir() {
        return draftsDir;
    }

    public boolean saveDraftFile(String name, String content) {
        name = sanitizeName(name);
        Path file = draftsDir.resolve(name + ".txt");
        try {
            Files.createDirectories(draftsDir);
            Files.write(file, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> listDraftNames() {
        try (Stream<Path> files = Files.list(draftsDir)) {
            return files
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> {
                        String fname = p.getFileName().toString();
                        return fname.substring(0, fname.length() - 4);
                    })
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public String readDraftContent(String name) {
        name = sanitizeName(name);
        Path file = draftsDir.resolve(name + ".txt");
        try {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean deleteDraftFile(String name) {
        name = sanitizeName(name);
        Path file = draftsDir.resolve(name + ".txt");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            return false;
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
            return formatDraftList();
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
                return formatDraftList();
            default:
                String name = parts[0];
                String content = cleaned.substring(name.length()).trim();
                if (content.isEmpty()) {
                    return "Usage:\n"
                         + "  draft save <name> <content>  — Save a draft\n"
                         + "  draft list                   — List all drafts\n"
                         + "  draft show <name>            — Show a draft\n"
                         + "  draft delete <name>          — Delete a draft";
                }
                return saveDraftFile(name, content)
                        ? "Draft \"" + sanitizeName(name) + "\" saved, sir."
                        : "Error saving draft.";
        }
    }

    private String handleSave(String[] parts) {
        if (parts.length < 3) {
            return "Usage: draft save <name> <content>\nExample: draft save meeting-notes Discuss Q3 roadmap";
        }
        return saveDraftFile(parts[1], parts[2])
                ? "Draft \"" + sanitizeName(parts[1]) + "\" saved, sir."
                : "Error saving draft.";
    }

    private String handleShow(String[] parts) {
        if (parts.length < 2) {
            return "Usage: draft show <name>";
        }
        String content = readDraftContent(parts[1]);
        if (content == null) {
            return "No draft found with name \"" + sanitizeName(parts[1]) + "\". Use 'draft list' to see saved drafts.";
        }
        return "Draft \"" + sanitizeName(parts[1]) + "\":\n\n" + content;
    }

    private String handleDelete(String[] parts) {
        if (parts.length < 2) {
            return "Usage: draft delete <name>";
        }
        return deleteDraftFile(parts[1])
                ? "Draft \"" + sanitizeName(parts[1]) + "\" deleted, sir."
                : "No draft found with name \"" + sanitizeName(parts[1]) + "\".";
    }

    private String formatDraftList() {
        List<String> names = listDraftNames();
        if (names.isEmpty()) {
            return "No drafts saved yet, sir. Use 'draft save <name> <content>' to create one.";
        }
        StringBuilder sb = new StringBuilder("Saved drafts:\n");
        for (String name : names) {
            sb.append("  - ").append(name).append("\n");
        }
        sb.append("\nUse 'draft show <name>' to view a draft.");
        return sb.toString();
    }

    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
