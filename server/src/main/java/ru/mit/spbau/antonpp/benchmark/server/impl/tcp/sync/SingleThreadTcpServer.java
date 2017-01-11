package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
public class SingleThreadTcpServer extends AbstractTcpServer {

    public SingleThreadTcpServer(int port) throws IOException {
        super(port, false);
    }

    @Override
    public void start() {
        startListeningLoop();
    }

    @Override
    protected void onConnection(Socket client) {
        handleConnection(client);
        AsynchronousServerSocketChannel channel;
    }
}
