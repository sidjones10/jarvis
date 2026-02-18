package com.jarvis;

import com.jarvis.commands.*;
import com.jarvis.core.JarvisEngine;
import com.jarvis.ui.JarvisUI;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JarvisEngine engine = new JarvisEngine();

        // Register all commands
        engine.registerCommand(new TimeCommand());
        engine.registerCommand(new SystemInfoCommand());
        engine.registerCommand(new CalculatorCommand());
        engine.registerCommand(new JokeCommand());
        engine.registerCommand(new WeatherCommand());
        engine.registerCommand(new QuoteCommand());
        engine.registerCommand(new SearchCommand());
        engine.registerCommand(new DraftCommand());

        ReminderCommand reminderCommand = new ReminderCommand();
        engine.registerCommand(reminderCommand);

        // Launch the UI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            JarvisUI ui = new JarvisUI(engine);
            reminderCommand.setResponseListener(ui);
            ui.setVisible(true);
        });
    }
}
