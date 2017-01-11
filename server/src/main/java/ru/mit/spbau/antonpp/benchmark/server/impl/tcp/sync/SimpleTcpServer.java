package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class SimpleTcpServer extends BlockingTcpServer {
    public SimpleTcpServer(int port) throws IOException {
        super(port,
                new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()),
                true);
    }
}
