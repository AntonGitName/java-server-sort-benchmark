package ru.mit.spbau.antonpp.benchmark.client;

import com.google.common.collect.Ordering;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.client.execeptions.InvalidServerResponse;
import ru.mit.spbau.antonpp.benchmark.client.execeptions.ServerUnavailableException;
import ru.mit.spbau.antonpp.benchmark.protocol.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * @author antonpp
 * @since 21/12/2016
 */
public class Client {

    private static final Random RND = new Random();

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static Message.Data generateArray(int n) {
        val builder = Message.Data.newBuilder();
        RND.ints(n).forEach(builder::addData);
        return builder.build();
    }

    private void send(DataInputStream dis, DataOutputStream dos, int arraySize) throws IOException {
        final Message.Data data = generateArray(arraySize);
        final byte[] bytes = data.toByteArray();
        dos.writeInt(bytes.length);
        dos.write(bytes);
        final int responseSize = dis.readInt();
        final byte[] responseData = new byte[responseSize];
        int offset = 0;
        int read = 0;
        while (read != responseSize) {
            read += dis.read(responseData, offset, responseSize - read);
        }
        final Message.Data response = Message.Data.parseFrom(responseData);
        if (!Ordering.natural().isOrdered(response.getDataList())) {
            throw new InvalidServerResponse("Array is not sorted");
        }
    }


    public long sendRequests(RequestConfig config) throws ServerUnavailableException, InterruptedException {
        final long start = System.currentTimeMillis();
        if (config.isKeepConnection()) {
            openConnection((dis, dos) -> {
                for (int i = 0; i < config.getNumRequests(); ++i) {
                    send(dis, dos, config.getArraySize());
                    Thread.sleep(config.getSendDelay());
                }
            });
        } else {
            for (int i = 0; i < config.getNumRequests(); ++i) {
                openConnection((dis, dos) -> send(dis, dos, config.getArraySize()));
                Thread.sleep(config.getSendDelay());
            }
        }
        final long end = System.currentTimeMillis();
        return end - start;
    }

    private void openConnection(ConnectionHandler handler) throws ServerUnavailableException, InterruptedException {
        try (Socket socket = new Socket(host, port);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            handler.handle(dis, dos);

        } catch (IOException e) {
            throw new ServerUnavailableException("Could not connect to server", e);
        }
    }

    private interface ConnectionHandler {
        void handle(DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException;
    }

    @Data
    @Builder
    public static class RequestConfig {
        private final boolean keepConnection;
        private final int numRequests;
        private final int arraySize;
        private final long sendDelay;
    }
}
