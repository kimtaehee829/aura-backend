package com.aura.aura.domain.analysis.entity;

public enum Mood {
    STREET,
    ROMANTIC,
    CLASSIC,
    MINIMAL;

    public static Mood from(String value) {
        if (value == null) return CLASSIC;
        try {
            return Mood.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CLASSIC;
        }
    }
}
