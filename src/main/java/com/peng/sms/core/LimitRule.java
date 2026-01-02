package com.peng.sms.core;

import java.util.concurrent.TimeUnit;

public record LimitRule(int limit, int window, TimeUnit timeUnit) {
    // the window in seconds
    public long getWindowInSeconds() {
        return timeUnit.toSeconds(window);
    }
}