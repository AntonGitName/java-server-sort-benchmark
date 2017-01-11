package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.server.TaskHandler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class ReadHandler implements CompletionHandler<Integer, ReadAttachment> {
    @Override
    public void completed(Integer result, ReadAttachment readAttachment) {
        if (readAttachment.isReadingSize()) {
            if (readAttachment.getSizeBuffer().hasRemaining()) {
                if (result != -1) {
                    readAttachment.getClientChannel().read(readAttachment.getSizeBuffer(), readAttachment, this);
                } else {
                    try {
                        readAttachment.getClientChannel().close();
                    } catch (IOException e) {
                        log.error("Failed to close empty channel");
                    }
                }
            } else {
                readAttachment.getSizeBuffer().flip();
                readAttachment.setNumsBuffer(ByteBuffer.allocate(readAttachment.getSizeBuffer().getInt()));
                readAttachment.getNumsBuffer().clear();
                readAttachment.setReadingSize(false);
                readAttachment.getClientChannel().read(readAttachment.getNumsBuffer(), readAttachment, this);
            }
        } else {
            if (readAttachment.getNumsBuffer().hasRemaining()) {
                if (result != -1) {
                    readAttachment.getClientChannel().read(readAttachment.getNumsBuffer(), readAttachment, this);
                } else {
                    try {
                        readAttachment.getClientChannel().close();
                    } catch (IOException e) {
                        log.error("Failed to close empty channel");
                    }
                }
            } else {
                readAttachment.getNumsBuffer().flip();
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    bos.write(readAttachment.getSizeBuffer().array());
                    bos.write(readAttachment.getNumsBuffer().array());
                } catch (IOException e) {
                    log.error("failed to copy bytes");
                }

                readAttachment.getServer().getExecutionService().execute(() -> {
                    try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         DataOutputStream dos = new DataOutputStream(baos)) {

                        TaskHandler.handle(dis, dos);


                        final WriteAttachment writeAttach = WriteAttachment.builder()
                                .startTime(readAttachment.getStartTime())
                                .server(readAttachment.getServer())
                                .buffer(ByteBuffer.wrap(baos.toByteArray()))
                                .clientChannel(readAttachment.getClientChannel())
                                .build();

                        readAttachment.getClientChannel().write(readAttachment.getSizeBuffer(), writeAttach, new WriteHandler());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        }
    }

    @Override
    public void failed(Throwable e, ReadAttachment attach) {
        log.error("Failed to read request from client", e);
        try {
            attach.getClientChannel().close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
