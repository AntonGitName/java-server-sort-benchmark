package ru.mit.spbau.antonpp.benchmark.server.impl.udp;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class SimpleUdpServer extends UdpServerWithExecutor {
    public SimpleUdpServer(int port) throws SocketException {
        super(port, new ThreadPoolExecutor(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));
    }
}
