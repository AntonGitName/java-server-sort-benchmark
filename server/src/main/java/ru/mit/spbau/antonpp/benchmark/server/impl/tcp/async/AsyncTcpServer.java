package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
public class AsyncTcpServer implements Server {

    @Getter(AccessLevel.PACKAGE)
    private final AsynchronousServerSocketChannel channel;
    @Getter(AccessLevel.PACKAGE)
    private final ExecutorService executionService = Executors.newCachedThreadPool();
    private boolean isRunning;

    private final List<Long> handleTimes = new ArrayList<>();

    public void addHandleTime(long time) {
        handleTimes.add(time);
    }

    public AsyncTcpServer(int port) throws IOException {
        channel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
    }

    @Override
    public void start() {
        isRunning = true;
        channel.accept(this, new ConnectionHandler());
    }

    @Override
    public double getAverageRequestHandleTime() {
        return handleTimes.stream().mapToLong(x -> x).average().orElse(0);
    }

    @Override
    public double getAverageClientServeTime() {
        return handleTimes.stream().mapToLong(x -> x).average().orElse(0);
    }

    @Override
    public void close() throws IOException {
        isRunning = false;
        executionService.shutdownNow();
        channel.close();
    }
}
