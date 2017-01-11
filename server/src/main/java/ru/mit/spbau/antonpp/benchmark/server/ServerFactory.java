package ru.mit.spbau.antonpp.benchmark.server;

import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async.AsyncTcpServer;
import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync.BlockingTcpServer;
import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync.NonBlockingTcpServer;
import ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync.SingleThreadTcpServer;

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
                return new BlockingTcpServer(port, new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()), false);
            case TCP_CACHED_POOL:
                return new BlockingTcpServer(port, Executors.newCachedThreadPool(), true);
            case TCP_NON_BLOCKING:
                return new NonBlockingTcpServer(port);
            case TCP_ASYNC:
                return new AsyncTcpServer(port);
            case TCP_ONE_THREAD:
                return new SingleThreadTcpServer(port);
            default:
                throw new IllegalArgumentException("Unknown server mode");
        }
    }
}
