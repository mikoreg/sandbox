package com.github.mikoreg.timelineaudio.domain;

public final class NoOpDebugLogger implements DebugLogger {
    @Override
    public void debug(String message) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void error(String message, Throwable error) {
    }
}
