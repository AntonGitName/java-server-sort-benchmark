package ru.mit.spbau.antonpp.benchmark.server.impl.tcp.async;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author antonpp
 * @since 21/12/2016
 */
@Slf4j
class Utils {

    private Utils() {
    }

    static void writeToClient(WriteAttachment attach, WriteHandler handler) {
        val buffer = attach.getBuffer();
        val data = attach.getData();
        val offset = attach.getOffset();
        val length = Math.min(data.length - offset, buffer.limit());
        buffer.clear();
        buffer.put(attach.getData(), offset, length);
        attach.setOffset(offset + length);
        attach.getClientChannel().write(buffer, attach, handler);
    }

    static void readFromClient(ReadAttachment attach, ReadHandler handler) {
        val buffer = attach.getBuffer();
        attach.getData().write(buffer.array(), 0, buffer.position());
        buffer.clear();
        attach.getClientChannel().read(buffer, attach, handler);
    }
}
