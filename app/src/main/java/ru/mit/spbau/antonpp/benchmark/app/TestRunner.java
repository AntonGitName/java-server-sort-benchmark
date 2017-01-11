package ru.mit.spbau.antonpp.benchmark.app;

import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.client.AbstractClient;
import ru.mit.spbau.antonpp.benchmark.client.execeptions.ServerUnavailableException;
import ru.mit.spbau.antonpp.benchmark.server.Server;
import ru.mit.spbau.antonpp.benchmark.server.ServerFactory;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static ru.mit.spbau.antonpp.benchmark.app.ParameterType.DELAY;
import static ru.mit.spbau.antonpp.benchmark.app.ParameterType.NUMBER_CLIENTS;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
@Slf4j
public class TestRunner {

    private static final int TEST_SERVER_PORT = 30001;
    private static final int SETUP_TIME_MS = 100;


    private final TestConfig config;

    public TestRunner(TestConfig config) {
        this.config = config;
    }

    private TestReport runTest(Server server, int numRequests, int numClients, ServerMode mode, int delay, int arraySize) throws TestExecutionException {
        final List<AbstractClient> clients = new ArrayList<>(numClients);
        IntStream.range(0, numClients).forEach(x -> clients.add(ClientFactory.create(mode, "localhost", TEST_SERVER_PORT)));
        final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.execute(server::start);

        final ExecutorService clientExecutor = Executors.newFixedThreadPool(numClients);
        final AbstractClient.RequestConfig requestConfig = AbstractClient.RequestConfig.builder()
                .numRequests(numRequests)
                .arraySize(arraySize)
                .sendDelay(delay)
                .build();

        log.info("Starting test: {}", requestConfig);

        final ExecutionException executionException[] = {null};
        final double averageClientWorkTime;

        try {

            List<Future<Long>> futures = new ArrayList<>(numClients);
            for (AbstractClient client : clients) {
                futures.add(clientExecutor.submit(() -> {
                    try {
                        return client.sendBenchmarkRequests(requestConfig);
                    } catch (ServerUnavailableException e) {
                        throw new TestExecutionException(e.getMessage(), e.getCause());
                    } catch (InterruptedException e) {
                        throw new TestExecutionException("Test executor was interrupted", e);
                    }
                }));
            }

            averageClientWorkTime = futures.stream().mapToLong(x -> {
                try {
                    return x.get();
                } catch (InterruptedException e) {
                    log.error("Interrupted during execution", e);
                    return Long.MAX_VALUE;
                } catch (ExecutionException e) {
                    log.error("Error during execution", e);
                    executionException[0] = e;
                    return Long.MAX_VALUE;
                }
            }).average().orElse(0);

        } finally {
            try {
                server.close();
            } catch (IOException e) {
                throw new TestExecutionException("could not close  server", e);
            }
            clientExecutor.shutdownNow();
            serverExecutor.shutdownNow();
        }

        if (executionException[0] != null) {
            throw new TestExecutionException(executionException[0].getMessage(), executionException[0].getCause());
        }


        log.info("test finished");

        return TestReport.builder()
                .clientWorkTime(averageClientWorkTime)
                .clientServeTime(server.getAverageClientServeTime())
                .requestHandleTime(server.getAverageRequestHandleTime())
                .build();
    }

    private TestReport runTest(int numRequests, int numClients, ServerMode mode, int delay, int arraySize, int i) throws TestExecutionException {

        int curTry = 0;
        Server server = null;
        IOException e3 = null;
        while (curTry++ < 5) {
            try {
                server = ServerFactory.create(TEST_SERVER_PORT, mode);
                break;
            } catch (IOException e) {
                try {
                    e3 = e;
                    Thread.sleep(SETUP_TIME_MS);
                } catch (InterruptedException e1) {
                    throw new TestExecutionException("interrupted", e1);
                }
            }
        }
        if (server != null) {

            return runTest(server, numRequests, numClients, mode, delay, arraySize);
        } else {
            throw new TestExecutionException("Could not start server", e3);
        }
    }

    public List<TestReport> run() throws TestExecutionException {

        final List<TestReport> reports = new ArrayList<>();

        for (int i = config.getParamLower(), j = 0; i < config.getParamUpper(); i += config.getParamStep()) {
            final int numClients;
            final int delay;
            final int arraySize;
            final ParameterType type = config.getParameterType();
            int param1 = config.getParam1();
            int param2 = config.getParam2();
            switch (type) {
                case DELAY:
                    delay = i;
                    if (type.getSecond() == NUMBER_CLIENTS) {
                        numClients = param1;
                        arraySize = param2;
                    } else {
                        arraySize = param1;
                        numClients = param2;
                    }
                    break;
                case ARRAY_SIZE:
                    arraySize = i;
                    if (type.getSecond() == NUMBER_CLIENTS) {
                        numClients = param1;
                        delay = param2;
                    } else {
                        delay = param1;
                        numClients = param2;
                    }
                    break;
                case NUMBER_CLIENTS:
                    numClients = i;
                    if (type.getSecond() == DELAY) {
                        delay = param1;
                        arraySize = param2;
                    } else {
                        arraySize = param1;
                        delay = param2;
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }

            reports.add(runTest(config.getNumRequests(), numClients, config.getMode(), delay, arraySize, j++));
        }
        return reports;
    }


}
