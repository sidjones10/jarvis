package com.jarvis.commands;

import com.jarvis.core.Command;
import java.util.Random;

public class QuoteCommand implements Command {

    private final Random random = new Random();

    private static final String[] QUOTES = {
        "\"The best way to predict the future is to invent it.\"\n  — Alan Kay",
        "\"Talk is cheap. Show me the code.\"\n  — Linus Torvalds",
        "\"Any sufficiently advanced technology is indistinguishable from magic.\"\n  — Arthur C. Clarke",
        "\"The only way to do great work is to love what you do.\"\n  — Steve Jobs",
        "\"First, solve the problem. Then, write the code.\"\n  — John Johnson",
        "\"Simplicity is the soul of efficiency.\"\n  — Austin Freeman",
        "\"Innovation distinguishes between a leader and a follower.\"\n  — Steve Jobs",
        "\"The computer was born to solve problems that did not exist before.\"\n  — Bill Gates",
        "\"Code is like humor. When you have to explain it, it's bad.\"\n  — Cory House",
        "\"Sometimes it is the people no one imagines anything of who do the things no one can imagine.\"\n  — Alan Turing"
    };

    @Override
    public String getName() {
        return "quote";
    }

    @Override
    public String getDescription() {
        return "Shares an inspirational quote.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"quote", "inspire", "motivation", "inspire me"};
    }

    @Override
    public String execute(String input) {
        return QUOTES[random.nextInt(QUOTES.length)];
    }
}
