package ru.mit.spbau.antonpp.benchmark.app;

import ru.mit.spbau.antonpp.benchmark.client.AbstractClient;
import ru.mit.spbau.antonpp.benchmark.client.TcpClient;
import ru.mit.spbau.antonpp.benchmark.client.UdpClient;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class ClientFactory {
    private ClientFactory() {
    }

    public static AbstractClient create(ServerMode mode, String host, int port) {
        switch (mode) {

            case TCP_THREAD_PER_CLIENT:
            case TCP_CACHED_POOL:
            case TCP_NON_BLOCKING:
            case TCP_ASYNC:
                return new TcpClient(host, port, true);
            case TCP_ONE_THREAD:
                return new TcpClient(host, port, false);
            case UDP_THREAD_PER_REQUEST:
            case UDP_FIXED_POOL:
                return new UdpClient(host, port);
            default:
                throw new IllegalArgumentException();
        }
    }
}
