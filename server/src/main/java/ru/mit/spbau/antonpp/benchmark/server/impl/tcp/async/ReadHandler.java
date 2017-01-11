package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.server.TaskHandler;

import java.io.*;
import java.nio.channels.CompletionHandler;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class ReadHandler implements CompletionHandler<Integer, ReadAttachment> {
    @Override
    public void completed(Integer result, ReadAttachment readAttachment) {
        if (result == -1) {
            readAttachment.getServer().getExecutionService().execute(() -> {
                final byte[] data = readAttachment.getData().toByteArray();

                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     DataOutputStream dos = new DataOutputStream(baos)) {

                    TaskHandler.handle(dis, dos);

                    final WriteAttachment writeAttach = WriteAttachment.builder()
                            .startTime(readAttachment.getStartTime())
                            .server(readAttachment.getServer())
                            .buffer(readAttachment.getBuffer())
                            .clientChannel(readAttachment.getClientChannel())
                            .offset(0)
                            .data(baos.toByteArray())
                            .build();

                    Utils.writeToClient(writeAttach, new WriteHandler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Utils.readFromClient(readAttachment, this);
        }
    }

    @Override
    public void failed(Throwable e, ReadAttachment attach) {
        log.error("Failed to read request from client", e);
    }
}
