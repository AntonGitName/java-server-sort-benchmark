package ru.mit.spbau.antonpp.benchmark.server;

/**
 * @author antonpp
 * @since 20/12/2016
 */
public enum ServerMode {
    TCP_THREAD_PER_CLIENT("TCP with thread per client", true),
    TCP_CACHED_POOL("TCP with cached thread pool", true),
    TCP_NON_BLOCKING("TCP non blocking", true),
    TCP_ASYNC("TCP async", false),
    TCP_ONE_THREAD("TCP single thread", false);

    private final String description;
    private final boolean keepConnection;

    ServerMode(String description, boolean keepConnection) {
        this.description = description;
        this.keepConnection = keepConnection;
    }

    @Override
    public String toString() {
        return description;
    }

    public boolean isKeepConnection() {
        return keepConnection;
    }
}
