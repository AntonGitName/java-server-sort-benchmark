package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author antonpp
 * @since 21/12/2016
 */
public class NonBlockingTcpServer extends TcpServerWithExecutor {

    private static final int MAX_THREADS = 8;

    public NonBlockingTcpServer(int port) throws IOException {
        super(port, Executors.newFixedThreadPool(MAX_THREADS), true);
        channel.configureBlocking(false);
    }

    @Override
    public void start() {
        setRunning(true);
        startListeningLoop();
    }
}
