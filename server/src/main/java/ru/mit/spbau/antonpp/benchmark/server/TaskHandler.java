package ru.mit.spbau.antonpp.benchmark.server;

import lombok.val;
import ru.mit.spbau.antonpp.benchmark.protocol.Message;
import ru.mit.spbau.antonpp.benchmark.protocol.Message.Data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author antonpp
 * @since 20/12/2016
 */
public class TaskHandler {

    private static Data sort(Data data) {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < data.getDataList().size() - 1; ++i) {
                if (data.getDataList().get(i) > data.getDataList().get(i + 1)) {
                    swap(data.getDataList(), i, i + 1);
                }
            }
        }
        return data;
    }

    private static void swap(List<Integer> list, int i, int j) {
        val tmp = list.get(j);
        list.set(j, list.get(i));
        list.set(i, tmp);
    }

    public static long handle(DataInputStream dis, DataOutputStream dos) throws IOException {
        final long start = System.currentTimeMillis();
        val length = dis.readInt();
        val data = new byte[length];
        int read = 0;
        while (read < length) {
            read += dis.read(data, read, length - read);
        }

        val input = Message.Data.parseFrom(data);
        val output = TaskHandler.sort(input);
        dos.write(output.toByteArray());
        final long end = System.currentTimeMillis();
        return end - start;
    }
}
