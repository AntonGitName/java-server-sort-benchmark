package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class CachedPoolTcpServer extends BlockingTcpServer {
    public CachedPoolTcpServer(int port) throws IOException {
        super(port, Executors.newCachedThreadPool(), true);
    }
}
