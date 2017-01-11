package ru.mit.spbau.antonpp.benchmark.server;

import java.io.Closeable;
import java.net.Socket;

/**
 * @author antonpp
 * @since 20/12/2016
 */
public interface Server extends Closeable {

    void start();
    double getAverageRequestHandleTime();
    double getAverageClientServeTime();

}
