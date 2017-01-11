package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author antonpp
 * @since 20/12/2016
 */
@Slf4j
public abstract class BlockingTcpServer extends TcpServerWithExecutor {

    private final ExecutorService listenService = Executors.newSingleThreadExecutor();

    @Override
    public void start() {
        listenService.execute(this::startListeningLoop);
    }

    protected BlockingTcpServer(int port, ExecutorService executionService, boolean keepConnection) throws IOException {
        super(port, executionService, keepConnection);
    }

    @Override
    public void close() throws IOException {
        listenService.shutdownNow();
        super.close();
    }
}
