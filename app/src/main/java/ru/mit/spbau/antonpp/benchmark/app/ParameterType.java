package ru.mit.spbau.antonpp.benchmark.app;

import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

/**
 * @author antonpp
 * @since 25/12/2016
 */
public enum ParameterType {
    DELAY("Delay"), ARRAY_SIZE("Array size"), NUMBER_CLIENTS("Number of clients");

    private final String description;

    ParameterType(String description) {
        this.description = description;
    }

    public ParameterType getSecond() {
        switch (this) {
            case DELAY:
                return ARRAY_SIZE;
            case ARRAY_SIZE:
            case NUMBER_CLIENTS:
                return DELAY;
            default:
                throw new IllegalStateException();
        }
    }

    public ParameterType getThird() {
        switch (this) {
            case DELAY:
            case ARRAY_SIZE:
                return NUMBER_CLIENTS;
            case NUMBER_CLIENTS:
                return ARRAY_SIZE;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return description;
    }

    public static ParameterType parse(String s) {
        for (final ParameterType x : values()) {
            if (s.equals(x.toString())) {
                return x;
            }
        }
        return null;

    }
}
