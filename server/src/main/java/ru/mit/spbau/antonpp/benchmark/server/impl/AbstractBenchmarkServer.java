package ru.mit.spbau.antonpp.benchmark.server.impl;

import ru.mit.spbau.antonpp.benchmark.server.Server;
import ru.mit.spbau.antonpp.benchmark.server.TaskHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public abstract class AbstractBenchmarkServer implements Server {

    private final BlockingQueue<Long> handleTimes = new LinkedBlockingQueue<>();
    protected final BlockingQueue<Long> serveTimes = new LinkedBlockingQueue<>();

    protected void benchmarkHandle(DataInputStream dis, DataOutputStream dos) throws IOException {
        handleTimes.add(TaskHandler.handle(dis, dos));
    }

    @Override
    public final double getAverageRequestHandleTime() {
        return handleTimes.stream().mapToLong(x -> x).average().orElse(0);
    }

    @Override
    public final double getAverageClientServeTime() {
        return serveTimes.stream().mapToLong(x -> x).average().orElse(0);
    }
}
