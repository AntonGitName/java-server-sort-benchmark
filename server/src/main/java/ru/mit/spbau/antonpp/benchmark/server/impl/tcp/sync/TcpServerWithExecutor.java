package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
public abstract class TcpServerWithExecutor extends AbstractTcpServer {
    private final ExecutorService executionService;

    protected TcpServerWithExecutor(int port, ExecutorService executionService, boolean keepConnection) throws IOException {
        super(port, keepConnection);
        this.executionService = executionService;
    }

    @Override
    public void onConnection(Socket client) {
        executionService.execute(() -> handleConnection(client));
    }

    @Override
    public void close() throws IOException {
        executionService.shutdownNow();
        super.close();
    }
}
