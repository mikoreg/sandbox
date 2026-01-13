package com.github.mikoreg.timelineaudio.app;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;

import java.time.Instant;

public class ConsoleDebugLogger implements DebugLogger {
    private final boolean debugEnabled;

    public ConsoleDebugLogger(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    @Override
    public void debug(String message) {
        if (debugEnabled) {
            System.out.println(timestamp() + " [DEBUG] " + message);
        }
    }

    @Override
    public void info(String message) {
        System.out.println(timestamp() + " [INFO] " + message);
    }

    @Override
    public void warn(String message) {
        System.out.println(timestamp() + " [WARN] " + message);
    }

    @Override
    public void error(String message, Throwable error) {
        System.out.println(timestamp() + " [ERROR] " + message);
        if (error != null) {
            error.printStackTrace(System.out);
        }
    }

    private String timestamp() {
        return Instant.now().toString();
    }
}
