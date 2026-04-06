package com.letsgettesty.backend.model;

import java.util.Arrays;

public enum NotificationChannel {
    EMAIL("email"),
    SMS("sms");

    private final String persistenceValue;

    NotificationChannel(String persistenceValue) {
        this.persistenceValue = persistenceValue;
    }

    public String getPersistenceValue() {
        return persistenceValue;
    }

    public static NotificationChannel fromPersistenceValue(String value) {
        return Arrays.stream(values())
                .filter(channel -> channel.persistenceValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported notification channel: " + value));
    }
}
