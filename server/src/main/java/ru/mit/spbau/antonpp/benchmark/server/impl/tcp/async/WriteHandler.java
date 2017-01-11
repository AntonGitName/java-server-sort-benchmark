package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class WriteHandler implements CompletionHandler<Integer, WriteAttachment>{
    @Override
    public void completed(Integer result, WriteAttachment attach) {
        if (attach.getBuffer().hasRemaining()) {
            attach.getClientChannel().write(attach.getBuffer(), attach, this);
        } else {
            attach.getServer().addHandleTime(System.currentTimeMillis() - attach.getStartTime());
            val attachment = ReadAttachment.builder()
                    .startTime(System.currentTimeMillis())
                    .server(attach.getServer())
                    .clientChannel(attach.getClientChannel())
                    .sizeBuffer(ConnectionHandler.createByteBuffer())
                    .readingSize(true)
                    .build();
            attach.getClientChannel().read(attachment.getSizeBuffer(), attachment, new ReadHandler());
        }
    }

    @Override
    public void failed(Throwable exc, WriteAttachment attachment) {
        log.error("Failed to write response to client", exc);
        try {
            attachment.getClientChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
