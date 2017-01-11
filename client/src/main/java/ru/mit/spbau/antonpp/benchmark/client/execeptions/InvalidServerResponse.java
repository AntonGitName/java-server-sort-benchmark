package ru.mit.spbau.antonpp.benchmark.client.execeptions;

import org.jetbrains.annotations.NonNls;

/**
 * @author antonpp
 * @since 25/12/2016
 */
public class InvalidServerResponse extends RuntimeException {
    public InvalidServerResponse(@NonNls String message) {
        super(message);
    }
}
