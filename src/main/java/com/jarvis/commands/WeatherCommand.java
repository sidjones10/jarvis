package com.jarvis.commands;

import com.jarvis.core.Command;
import java.util.Random;

public class WeatherCommand implements Command {

    private final Random random = new Random();

    private static final String[] CONDITIONS = {
        "Partly cloudy", "Clear skies", "Light rain expected",
        "Overcast", "Sunny with mild winds", "Thunderstorms possible"
    };

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public String getDescription() {
        return "Shows a simulated weather report.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"weather", "forecast", "temperature", "what's the weather"};
    }

    @Override
    public String execute(String input) {
        int temp = 60 + random.nextInt(35);
        String condition = CONDITIONS[random.nextInt(CONDITIONS.length)];
        int humidity = 30 + random.nextInt(50);

        return String.format(
            "Current weather report, sir:\n\n" +
            "  Condition:    %s\n" +
            "  Temperature:  %d°F / %.1f°C\n" +
            "  Humidity:     %d%%\n\n" +
            "Note: This is a simulated report. Connect a weather API for live data.",
            condition, temp, (temp - 32) * 5.0 / 9.0, humidity
        );
    }
}
