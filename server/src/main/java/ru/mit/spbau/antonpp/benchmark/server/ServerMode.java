package ru.mit.spbau.antonpp.benchmark.server;

/**
 * @author antonpp
 * @since 20/12/2016
 */
public enum ServerMode {
    TCP_THREAD_PER_CLIENT("TCP with thread per client"),
    TCP_CACHED_POOL("TCP with cached thread pool"),
    TCP_NON_BLOCKING("TCP non blocking"),
    TCP_ASYNC("TCP async"),
    TCP_ONE_THREAD("TCP single thread"),

    UDP_THREAD_PER_REQUEST("UDP thread per request"),
    UDP_FIXED_POOL("UDP fixed pool");

    private final String description;

    ServerMode(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static ServerMode parse(String s) {
        for (final ServerMode serverMode : values()) {
            if (s.equals(serverMode.toString())) {
                return serverMode;
            }
        }
        return null;
    }
}
