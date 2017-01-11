package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;

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
        if (result == -1 || attach.getOffset() == attach.getData().length) {
            try {
                attach.getServer().addHandleTime(System.currentTimeMillis() - attach.getStartTime());
                attach.getClientChannel().close();
            } catch (IOException e) {
                log.error("Failed to close client channel", e);
            }
        } else {
            attach.getBuffer().flip();
            Utils.writeToClient(attach, this);
        }
    }

    @Override
    public void failed(Throwable exc, WriteAttachment attachment) {
        log.error("Failed to write response to client", exc);
    }
}
