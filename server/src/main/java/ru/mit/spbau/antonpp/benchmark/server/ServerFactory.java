package ru.mit.spbau.antonpp.benchmark.server;

import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async.AsyncTcpServer;
import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync.*;
import ru.mit.spbau.antonpp.benchmark.server.impl.udp.FixedPoolUdpServer;
import ru.mit.spbau.antonpp.benchmark.server.impl.udp.SimpleUdpServer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author antonpp
 * @since 21/12/2016
 */
public class ServerFactory {
    public static Server create(int port, ServerMode mode) throws IOException {
        switch (mode) {
            case TCP_THREAD_PER_CLIENT:
                return new SimpleTcpServer(port);
            case TCP_CACHED_POOL:
                return new CachedPoolTcpServer(port);
            case TCP_NON_BLOCKING:
                return new NonBlockingTcpServer(port);
            case TCP_ASYNC:
                return new AsyncTcpServer(port);
            case TCP_ONE_THREAD:
                return new SingleThreadTcpServer(port);
            case UDP_FIXED_POOL:
                return new FixedPoolUdpServer(port);
            case UDP_THREAD_PER_REQUEST:
                return new SimpleUdpServer(port);
            default:
                throw new IllegalArgumentException("Unknown server mode");
        }
    }
}
