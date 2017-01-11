package ru.mit.spbau.antonpp.benchmark.server.impl.udp;

import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;
import java.util.concurrent.Executors;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
@Slf4j
public class FixedPoolUdpServer extends UdpServerWithExecutor {

    private static final int MAX_THREADS = 8;

    public FixedPoolUdpServer(int port) throws SocketException {
        super(port, Executors.newFixedThreadPool(MAX_THREADS));
    }
}
