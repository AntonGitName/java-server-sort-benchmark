package ru.mit.spbau.antonpp.benchmark.app;

import lombok.Builder;
import lombok.Data;

/**
 * @author antonpp
 * @since 25/12/2016
 */
@Data
@Builder
public class TestReport {
    private final double requestHandleTime;
    private final double clientServeTime;
    private final double clientWorkTime;
}
