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
import java.util.Random;

/**
 * @author antonpp
 * @since 21/12/2016
 */
public abstract class AbstractClient {
//
//    public static void main(String[] args) {
//        AbstractClient client = new AbstractClient("localhost", 31001);
//        try {
//            client.sendRequests(RequestConfig.builder().keepConnection(false).arraySize(10).numRequests(10).build());
//        } catch (ServerUnavailableException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


    private static final Random RND = new Random();

    protected final String host;
    protected final int port;

    public AbstractClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static Message.Data generateArray(int n) {
        val builder = Message.Data.newBuilder();
        RND.ints(n).forEach(builder::addData);
        return builder.build();
    }

    protected void write(DataOutputStream dos, int arraySize) throws IOException {
        final Message.Data data = generateArray(arraySize);
        final byte[] bytes = data.toByteArray();
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    protected void read(DataInputStream dis) throws IOException {
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


    protected abstract void sendRequests(RequestConfig config) throws ServerUnavailableException, InterruptedException;

    public final long sendBenchmarkRequests(RequestConfig config) throws ServerUnavailableException, InterruptedException {
        final long start = System.currentTimeMillis();
        sendRequests(config);

        final long end = System.currentTimeMillis();
        return end - start;
    }

    @Data
    @Builder
    public static class RequestConfig {
        private final int numRequests;
        private final int arraySize;
        private final long sendDelay;
    }
}
