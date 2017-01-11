package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Data @Builder
class ReadAttachment {
    private final long startTime;
    private final AsyncTcpServer server;
    private final AsynchronousSocketChannel clientChannel;
    private ByteBuffer sizeBuffer;
    private ByteBuffer numsBuffer;
    private int size;
    private boolean readingSize;
}
