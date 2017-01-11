package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.Builder;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Data
@Builder
class WriteAttachment {
    private final long startTime;
    private final AsyncTcpServer server;
    private final AsynchronousSocketChannel clientChannel;
    private final ByteBuffer buffer;
    private final byte[] data;
    private int offset;
}
