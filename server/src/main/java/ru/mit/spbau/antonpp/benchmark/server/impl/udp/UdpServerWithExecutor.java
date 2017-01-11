package ru.mit.spbau.antonpp.benchmark.server.impl.udp;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.server.impl.AbstractBenchmarkServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
@Slf4j
public class UdpServerWithExecutor extends AbstractBenchmarkServer {

    private static final int BUFFERS_SZ = 1024 * 1024;

    private final ExecutorService listenService = Executors.newSingleThreadExecutor();
    private final DatagramSocket socket;
    private final ExecutorService executionService;
    protected UdpServerWithExecutor(int port, ExecutorService executionService) throws SocketException {
        socket = new DatagramSocket(port);
        this.executionService = executionService;
        socket.setSendBufferSize(BUFFERS_SZ);
        socket.setReceiveBufferSize(BUFFERS_SZ);
    }

    @Override
    public void start() {
        listenService.execute(this::startListeningLoop);
    }

    @Override
    public final void close() throws IOException {
        socket.close();
        listenService.shutdownNow();
        executionService.shutdownNow();
    }

    public final void startListeningLoop() {
        while (!socket.isClosed()) {
            try {
                socket.setSoTimeout(30000);
                final byte[] inputBuffer = new byte[4096];
                val packet = new DatagramPacket(inputBuffer, inputBuffer.length);
                socket.receive(packet);
                final long start = System.currentTimeMillis();
                handleRequest(packet);
                final long end = System.currentTimeMillis();
                serveTimes.add(end - start);
            } catch (IOException e) {
                log.warn("Could not receive packet.", e);
            }
        }
    }

    private void handleRequest(DatagramPacket packet) {
        executionService.execute(() -> handleConnection(packet));
    }

    private void handleConnection(DatagramPacket packet) {
        final long start = System.currentTimeMillis();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData()));
             ByteArrayOutputStream boas = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(boas)) {

            benchmarkHandle(dis, dos);
            byte[] outputBuffer = boas.toByteArray();
            DatagramPacket response = new DatagramPacket(outputBuffer, outputBuffer.length);
            response.setAddress(packet.getAddress());
            response.setPort(packet.getPort());
            socket.send(response);
        } catch (IOException e) {
            log.warn("Failed to handle request", e);
        }
        final long end = System.currentTimeMillis();
        serveTimes.add(end - start);
    }
}
