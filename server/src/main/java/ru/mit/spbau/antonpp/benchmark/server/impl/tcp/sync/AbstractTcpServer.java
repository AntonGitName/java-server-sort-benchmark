package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.server.impl.AbstractBenchmarkServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
public abstract class AbstractTcpServer extends AbstractBenchmarkServer {

    protected final ServerSocketChannel channel;
    private final boolean keepConnection;

    protected AbstractTcpServer(int port, boolean keepConnection) throws IOException {
        this.keepConnection = keepConnection;
        channel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
    }

    protected abstract void onConnection(Socket client);

    @Override
    public void close() throws IOException {
        channel.close();
    }

    protected void startListeningLoop() {
        try {
            while (channel.isOpen()) {
                SocketChannel clientChannel = channel.accept();
                if (clientChannel != null) {
                    onConnection(clientChannel.socket());
                }
            }
        } catch (IOException e) {
            log.error("Could not accept new connections", e);
        }

    }

    protected void handleConnection(Socket client) {
        try (DataOutputStream dos = new DataOutputStream(client.getOutputStream());
             DataInputStream dis = new DataInputStream(client.getInputStream())) {
            final long start = System.currentTimeMillis();
            while (true) {
                benchmarkHandle(dis, dos);
                if (!keepConnection || client.isClosed()) {
                    client.close();
                    break;
                }
            }
            final long end = System.currentTimeMillis();
            serveTimes.add(end - start);
        } catch (IOException e) {
            log.warn("Failed to handle request", e);
        }
    }
}
