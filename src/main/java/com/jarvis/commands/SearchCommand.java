package com.jarvis.commands;

import com.jarvis.core.Command;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SearchCommand implements Command {

    @Override
    public String getName() {
        return "search <query>";
    }

    @Override
    public String getDescription() {
        return "Opens a web search in your default browser.";
    }

    @Override
    public String[] getTriggers() {
        return new String[]{"search ", "google ", "look up ", "find info "};
    }

    @Override
    public String execute(String input) {
        String query = input.replaceFirst("(?i)(search|google|look up|find info)\\s+", "").trim();
        if (query.isEmpty()) {
            return "What would you like me to search for, sir?";
        }

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://www.google.com/search?q=" + encoded;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                return "Opening search results for \"" + query + "\" in your browser, sir.";
            } else {
                return "I can't open the browser directly, but here's the search URL:\n" + url;
            }
        } catch (Exception e) {
            return "I encountered an issue opening the search, sir. Please try again.";
        }
    }
}
