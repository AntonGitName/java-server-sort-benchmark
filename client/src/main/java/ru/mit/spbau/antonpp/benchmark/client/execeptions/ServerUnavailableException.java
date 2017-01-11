package ru.mit.spbau.antonpp.benchmark.client.execeptions;

import java.io.IOException;

/**
 * @author antonpp
 * @since 25/12/2016
 */
public class ServerUnavailableException extends IOException {
    public ServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
