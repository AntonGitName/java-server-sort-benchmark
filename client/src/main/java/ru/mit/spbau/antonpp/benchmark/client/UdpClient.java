package ru.mit.spbau.antonpp.benchmark.client;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.client.execeptions.ServerUnavailableException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
@Slf4j
public class UdpClient extends AbstractClient {

    public UdpClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void sendRequests(RequestConfig config) throws ServerUnavailableException, InterruptedException {
        try (DatagramSocket socket = new DatagramSocket()) {
            for (int i = 0; i < config.getNumRequests(); ++i) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     DataOutputStream dos = new DataOutputStream(baos)) {
                    write(dos, config.getArraySize());
                    val requestBuffer = baos.toByteArray();
                    val request = new DatagramPacket(requestBuffer, requestBuffer.length);
                    request.setAddress(InetAddress.getByName(host));
                    request.setPort(port);
                    socket.send(request);
                    val responseBuffer = new byte[requestBuffer.length * 2];
                    val response = new DatagramPacket(responseBuffer, responseBuffer.length);
                    socket.receive(response);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(response.getData());
                         DataInputStream dis = new DataInputStream(bais)) {
                        read(dis);
                    }
                }
                Thread.sleep(config.getSendDelay());
            }
        } catch (IOException e) {
            throw new ServerUnavailableException("failed to send requests", e);
        }
    }
}
