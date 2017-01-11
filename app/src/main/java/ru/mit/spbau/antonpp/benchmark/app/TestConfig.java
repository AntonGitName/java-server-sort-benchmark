package ru.mit.spbau.antonpp.benchmark.app;

import lombok.Builder;
import lombok.Data;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

/**
 * @author antonpp
 * @since 25/12/2016
 */
@Data @Builder
public class TestConfig {
    private final ServerMode mode;
    private final int numRequests;
    private final ParameterType parameterType;
    private final int paramLower;
    private final int paramUpper;
    private final int paramStep;
    private final int param1;
    private final int param2;
}
