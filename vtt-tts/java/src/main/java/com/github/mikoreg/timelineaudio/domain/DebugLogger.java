package com.github.mikoreg.timelineaudio.domain;

public interface DebugLogger {
    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message, Throwable error);

    default void error(String message) {
        error(message, null);
    }
}
