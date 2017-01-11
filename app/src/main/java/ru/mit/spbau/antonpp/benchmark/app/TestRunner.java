package ru.mit.spbau.antonpp.benchmark.app;

import lombok.extern.slf4j.Slf4j;
import ru.mit.spbau.antonpp.benchmark.client.Client;
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

    private final TestConfig config;

    public TestRunner(TestConfig config) {
        this.config = config;
    }

    private TestReport runTest(int numRequests, int numClients, ServerMode mode, int delay, int arraySize) throws TestExecutionException {
        try (Server server = ServerFactory.create(8081, mode)) {
            final List<Client> clients = new ArrayList<>(numClients);
            IntStream.range(0, numClients).forEach(x -> clients.add(new Client("localhost", 31001 + x)));
            final ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.execute(server::start);
            final ExecutorService clientExecutor = Executors.newFixedThreadPool(numClients);
            final Client.RequestConfig requestConfig = Client.RequestConfig.builder()
                    .numRequests(numRequests)
                    .arraySize(arraySize)
                    .sendDelay(delay)
                    .keepConnection(mode.isKeepConnection())
                    .build();

            List<Future<Long>> futures = new ArrayList<>(numClients);
            for (Client client : clients) {
                futures.add(clientExecutor.submit(() -> {
                    try {
                        return client.sendRequests(requestConfig);
                    } catch (ServerUnavailableException e) {
                        throw new TestExecutionException(e.getMessage(), e.getCause());
                    } catch (InterruptedException e) {
                        throw new TestExecutionException("Test executor was interrupted", e);
                    }
                }));
            }
            final double averageClientWorkTime = futures.stream().mapToLong(x -> {
                try {
                    return x.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error during execution", e);
                    return Long.MAX_VALUE;
                }
            }).average().orElse(0);

            clientExecutor.shutdownNow();
            serverExecutor.shutdownNow();

            return TestReport.builder()
                    .clientWorkTime(averageClientWorkTime)
                    .clientServeTime(server.getAverageClientServeTime())
                    .requestHandleTime(server.getAverageRequestHandleTime())
                    .build();
        } catch (IOException e) {
            throw new TestExecutionException("Could not start/stop server", e);
        }
    }

    public List<TestReport> run() throws TestExecutionException {

        final List<TestReport> reports = new ArrayList<>();

        for (int i = config.getParamLower(); i < config.getParamUpper(); i += config.getParamStep()) {
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

            reports.add(runTest(config.getNumRequests(), numClients, config.getMode(), delay, arraySize));
        }
        return reports;
    }


}
