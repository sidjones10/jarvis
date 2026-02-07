package com.jarvis.core;

@FunctionalInterface
public interface ResponseListener {
    void onResponse(String response);
}
