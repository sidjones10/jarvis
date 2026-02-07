package com.jarvis.commands;

import com.jarvis.core.Command;
import java.util.Random;

public class JokeCommand implements Command {

    private final Random random = new Random();

    private static final String[] JOKES = {
        "Why do programmers prefer dark mode?\nBecause light attracts bugs, sir.",
        "There are only 10 types of people in the world:\nthose who understand binary, and those who don't.",
        "A SQL query walks into a bar, sees two tables and asks...\n'Can I join you?'",
        "Why was the JavaScript developer sad?\nBecause he didn't Node how to Express himself.",
        "What's a programmer's favorite hangout place?\nFoo Bar.",
        "Why do Java developers wear glasses?\nBecause they can't C#.",
        "How many programmers does it take to change a light bulb?\nNone. That's a hardware problem.",
        "What did the router say to the doctor?\nIt hurts when IP.",
        "Why did the developer go broke?\nBecause he used up all his cache.",
        "I told my wife she was drawing her eyebrows too high.\nShe looked surprised."
    };

    @Override
    public String getName() {
        return "joke";
    }

    @Override
    public String getDescription() {
        return "Tells a joke to lighten the mood.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"joke", "tell me a joke", "make me laugh", "funny"};
    }

    @Override
    public String execute(String input) {
        return JOKES[random.nextInt(JOKES.length)];
    }
}
