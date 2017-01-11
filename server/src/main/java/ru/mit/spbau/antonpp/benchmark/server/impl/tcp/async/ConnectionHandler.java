package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTcpServer> {

    private static final int BUFFER_SIZE = 4096;

    @Override
    public void completed(AsynchronousSocketChannel client, AsyncTcpServer server) {

        server.getChannel().accept(server, this);

        val attachment = ReadAttachment.builder()
                    .startTime(System.currentTimeMillis())
                    .server(server)
                    .clientChannel(client)
                    .buffer(createByteBuffer())
                    .data(new ByteArrayOutputStream())
                    .build();
            client.read(attachment.getBuffer(), attachment, new ReadHandler());
    }

    @Override
    public void failed(Throwable e, AsyncTcpServer attachment) {
        log.error("Failed to handle connection", e);
    }

    private static ByteBuffer createByteBuffer() {
        return ByteBuffer.allocate(BUFFER_SIZE);
    }
}
