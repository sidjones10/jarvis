package com.jarvis.core;

/**
 * Interface for all Jarvis commands.
 */
public interface Command {
    String getName();
    String getDescription();
    String[] getTriggers();
    String execute(String input);
}
