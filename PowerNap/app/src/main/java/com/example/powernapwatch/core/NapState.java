package com.example.powernapwatch.core;

public enum NapState {
    IDLE,
    BASELINE,
    WAITING_FOR_SLEEP,
    SLEEP_CANDIDATE,
    SLEEP_CONFIRMED,
    NAPPING,
    ALARMING,
    FINISHED,
    CANCELLED,
    ERROR
}
