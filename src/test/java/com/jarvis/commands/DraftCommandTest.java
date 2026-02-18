package com.jarvis.commands;

import java.nio.file.*;
import java.util.List;

public class DraftCommandTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        DraftCommand cmd = new DraftCommand();

        // Clean up before test
        cmd.deleteDraftFile("test-draft");
        cmd.deleteDraftFile("draft-one");
        cmd.deleteDraftFile("draft-two");

        testDraftsDirExists(cmd);
        testSaveDraftFile(cmd);
        testSaveAndReadBack(cmd);
        testListDraftNames(cmd);
        testDeleteDraft(cmd);
        testExecuteSaveCommand(cmd);
        testSaveOverwrite(cmd);

        // Clean up after test
        cmd.deleteDraftFile("test-draft");
        cmd.deleteDraftFile("draft-one");
        cmd.deleteDraftFile("draft-two");

        System.out.println("\n=============================");
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    static void testDraftsDirExists(DraftCommand cmd) {
        Path dir = cmd.getDraftsDir();
        check("Drafts dir exists", Files.exists(dir), "Dir: " + dir);
        check("Drafts dir is directory", Files.isDirectory(dir), "Dir: " + dir);
    }

    static void testSaveDraftFile(DraftCommand cmd) {
        boolean saved = cmd.saveDraftFile("test-draft", "Hello World");
        check("saveDraftFile returns true", saved, "returned: " + saved);

        Path file = cmd.getDraftsDir().resolve("test-draft.txt");
        check("Draft file exists on disk", Files.exists(file), "Path: " + file);
    }

    static void testSaveAndReadBack(DraftCommand cmd) {
        String content = "This is my draft content.";
        cmd.saveDraftFile("test-draft", content);

        String readBack = cmd.readDraftContent("test-draft");
        check("Read back is not null", readBack != null, "readBack: " + readBack);
        check("Content matches", content.equals(readBack),
                "expected: '" + content + "', got: '" + readBack + "'");
    }

    static void testListDraftNames(DraftCommand cmd) {
        cmd.saveDraftFile("draft-one", "Content one");
        cmd.saveDraftFile("draft-two", "Content two");

        List<String> names = cmd.listDraftNames();
        check("List contains draft-one", names.contains("draft-one"), "names: " + names);
        check("List contains draft-two", names.contains("draft-two"), "names: " + names);
    }

    static void testDeleteDraft(DraftCommand cmd) {
        cmd.saveDraftFile("test-draft", "To be deleted");
        check("Exists before delete",
                Files.exists(cmd.getDraftsDir().resolve("test-draft.txt")), "");

        boolean deleted = cmd.deleteDraftFile("test-draft");
        check("deleteDraftFile returns true", deleted, "returned: " + deleted);
        check("Not exists after delete",
                !Files.exists(cmd.getDraftsDir().resolve("test-draft.txt")), "");
    }

    static void testExecuteSaveCommand(DraftCommand cmd) {
        String result = cmd.execute("draft save test-draft Hello from command");
        check("Execute returns saved message", result.contains("saved"), "result: " + result);

        String readBack = cmd.readDraftContent("test-draft");
        check("Command saved correct content",
                "Hello from command".equals(readBack),
                "expected: 'Hello from command', got: '" + readBack + "'");
    }

    static void testSaveOverwrite(DraftCommand cmd) {
        cmd.saveDraftFile("test-draft", "Version 1");
        check("First save", "Version 1".equals(cmd.readDraftContent("test-draft")), "");

        cmd.saveDraftFile("test-draft", "Version 2");
        check("Overwrite save", "Version 2".equals(cmd.readDraftContent("test-draft")), "");
    }

    static void check(String name, boolean condition, String detail) {
        if (condition) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name + " (" + detail + ")");
            failed++;
        }
    }
}
