package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.sync;

import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.server.Server;
import ru.mit.spbau.antonpp.benchmark.server.TaskHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
public abstract class AbstractTcpServer implements Server {

    protected final ServerSocketChannel channel;
    private final boolean keepConnection;
    private boolean isRunning;
    private final List<Long> handleTimes = new ArrayList<>();
    private final List<Long> serveTimes = new ArrayList<>();

    protected AbstractTcpServer(int port, boolean keepConnection) throws IOException {
        this.keepConnection = keepConnection;
        channel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
    }

    protected abstract void onConnection(Socket client);

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void close() throws IOException {
        isRunning = false;
        channel.close();
    }

    protected void startListeningLoop() {
        try {
            while (isRunning()) {
                try (final SocketChannel socketChannel = channel.accept()) {
                    if (socketChannel != null) {
                        onConnection(socketChannel.socket());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not accept new connections", e);
            isRunning = false;
        }

    }

    protected void handleConnection(Socket client) {
        try (DataOutputStream dos = new DataOutputStream(client.getOutputStream());
             DataInputStream dis = new DataInputStream(client.getInputStream())) {
            final long start = System.currentTimeMillis();
            while (true) {
                handleTimes.add(TaskHandler.handle(dis, dos));
                if (!keepConnection) {
                    break;
                }
            }
            final long end = System.currentTimeMillis();
            serveTimes.add(end - start);
        } catch (IOException e) {
            log.warn("Failed to handle request", e);
        }
    }

    @Override
    public double getAverageRequestHandleTime() {
        return handleTimes.stream().mapToLong(x -> x).average().orElse(0);
    }

    @Override
    public double getAverageClientServeTime() {
        return serveTimes.stream().mapToLong(x -> x).average().orElse(0);
    }
}
