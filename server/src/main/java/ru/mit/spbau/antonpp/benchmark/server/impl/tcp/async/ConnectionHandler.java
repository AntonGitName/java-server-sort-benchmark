package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTcpServer> {

    private static final int INT_SIZE = 4;

    public static ByteBuffer createByteBuffer() {
        return ByteBuffer.allocate(INT_SIZE);
    }

    @Override
    public void completed(AsynchronousSocketChannel client, AsyncTcpServer server) {

        server.getChannel().accept(server, this);

        val attachment = ReadAttachment.builder()
                    .startTime(System.currentTimeMillis())
                    .server(server)
                    .clientChannel(client)
                .sizeBuffer(createByteBuffer())
                .readingSize(true)
                    .build();
        client.read(attachment.getSizeBuffer(), attachment, new ReadHandler());
    }

    @Override
    public void failed(Throwable e, AsyncTcpServer attachment) {
        log.error("Failed to handle connection", e);
        try {
            attachment.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
