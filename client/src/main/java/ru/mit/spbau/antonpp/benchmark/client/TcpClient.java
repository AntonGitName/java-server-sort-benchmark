package ru.mit.spbau.antonpp.benchmark.client;

import ru.mit.spbau.antonpp.benchmark.client.execeptions.ServerUnavailableException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class TcpClient extends AbstractClient {

    private final boolean keepConnection;

    public TcpClient(String host, int port, boolean keepConnection) {
        super(host, port);
        this.keepConnection = keepConnection;
    }

    protected void send(DataInputStream dis, DataOutputStream dos, int arraySize) throws IOException {
        write(dos, arraySize);
        read(dis);
    }

    @Override
    public void sendRequests(RequestConfig config) throws ServerUnavailableException, InterruptedException {
        if (keepConnection) {
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
    }

    private void openConnection(ConnectionHandler handler) throws ServerUnavailableException, InterruptedException {
        try (Socket socket = new Socket(host, port);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            handler.handle(dis, dos);

        } catch (IOException e) {
            throw new ServerUnavailableException(e.getMessage(), e);
        }
    }

    private interface ConnectionHandler {
        void handle(DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException;
    }
}
