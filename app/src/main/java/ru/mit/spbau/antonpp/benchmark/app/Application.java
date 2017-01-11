package ru.mit.spbau.antonpp.benchmark.app;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.app.panels.ResultTableModel;
import ru.mit.spbau.antonpp.benchmark.client.Client;
import ru.mit.spbau.antonpp.benchmark.client.execeptions.ServerUnavailableException;
import ru.mit.spbau.antonpp.benchmark.server.Server;
import ru.mit.spbau.antonpp.benchmark.server.ServerFactory;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static ru.mit.spbau.antonpp.benchmark.app.ParameterType.DELAY;
import static ru.mit.spbau.antonpp.benchmark.app.ParameterType.NUMBER_CLIENTS;

/**
 * @author antonpp
 * @since 25/12/2016
 */
@Slf4j
public class Application extends JFrame {

    private static final Path RESULTS_DIR = Paths.get("results");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Application::new);
    }

    public Application() {
        super("Java Sockets Benchmark");

        this.setBounds(100, 100, 740, 580);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {

            }
        });

        createUIElements();

        setVisible(true);
    }

    private void createUIElements() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        final ResultTableModel tableModel = new ResultTableModel();
        final JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane);
        final JButton button = new JButton("Create new Test");
        panel.add(button);
        button.addActionListener(e -> onCreateTest());
        add(panel);
    }

    private void onCreateTest() {
        final JComboBox<ServerMode> comboBox = new JComboBox<>(ServerMode.values());
        final JComboBox<ParameterType> comboBox2 = new JComboBox<>(ParameterType.values());
        final JLabel label1 = new JLabel();
        final JLabel label2 = new JLabel();
        final JLabel label3 = new JLabel();
        final JLabel label4 = new JLabel();
        final JTextField param1 = new JTextField();
        final JTextField param2 = new JTextField();
        final JTextField param3 = new JTextField();
        final JTextField param4 = new JTextField();
        final JTextField step = new JTextField();

        final Consumer<ParameterType> f = item -> {
            label1.setText(item + " lower bound:");
            label2.setText(item + " upper bound:");
            label3.setText(item.getSecond().toString());
            label4.setText(item.getThird().toString());
        };

        comboBox2.setSelectedItem(NUMBER_CLIENTS);
        f.accept(NUMBER_CLIENTS);

        comboBox2.addItemListener(e -> f.accept((ParameterType) e.getItem()));

        final JTextField requests = new JTextField("10");
        final JComponent[] inputs = new JComponent[]{
                new JLabel("Configuration:"),
                comboBox,
                new JLabel("Changing parameter:"),
                comboBox2,
                label1,
                param1,
                label2,
                param2,
                new JLabel("Step:"),
                step,
                label3,
                param3,
                label4,
                param4,
                new JLabel("Number of requests"),
                requests
        };

        int result = JOptionPane.showConfirmDialog(this, inputs, "Create test", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                final TestConfig config = TestConfig.builder()
                        .numRequests(Integer.valueOf(requests.getText()))
                        .mode((ServerMode) comboBox.getSelectedItem())
                        .param1(Integer.valueOf(param3.getText()))
                        .param2(Integer.valueOf(param4.getText()))
                        .paramLower(Integer.valueOf(param1.getText()))
                        .paramUpper(Integer.valueOf(param2.getText()))
                        .paramStep(Integer.valueOf(step.getText()))
                        .build();
                startTests(config);
            } catch (NumberFormatException e) {
                handleRecoverableError("Could not parse parameters", e);
            }
        }
    }

    private void startTests(TestConfig config) {
        final List<TestReport> reports = new ArrayList<>();
        for (int i = config.getParamLower(); i < config.getParamUpper(); i += config.getParamStep()) {
            final int numClients;
            final int delay;
            final int arraySize;
            final ParameterType type = config.getParameterType();
            switch (type) {
                case DELAY:
                    delay = i;
                    if (type.getSecond() == NUMBER_CLIENTS) {
                        numClients = config.getParam1();
                        arraySize = config.getParam2();
                    } else {
                        arraySize = config.getParam1();
                        numClients = config.getParam2();
                    }
                    break;
                case ARRAY_SIZE:
                    arraySize = i;
                    if (type.getSecond() == NUMBER_CLIENTS) {
                        numClients = config.getParam1();
                        delay = config.getParam2();
                    } else {
                        delay = config.getParam1();
                        numClients = config.getParam2();
                    }
                    break;
                case NUMBER_CLIENTS:
                    numClients = i;
                    if (type.getSecond() == DELAY) {
                        delay = config.getParam1();
                        arraySize = config.getParam2();
                    } else {
                        arraySize = config.getParam1();
                        delay = config.getParam2();
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
            reports.add(runTest(config.getNumRequests(), numClients, config.getMode(), delay, arraySize));
        }
    }

    private static void saveReports(List<TestReport> reports, TestConfig config) {
        try {
            final String testName = UUID.randomUUID().toString();
            try (PrintWriter pw = new PrintWriter(RESULTS_DIR.resolve("CST-" + testName).toFile())) {
                printHeader(pw, config);
                for (TestReport report : reports) {
                    pw.println(report.getClientServeTime());
                }
            }
            try (PrintWriter pw = new PrintWriter(RESULTS_DIR.resolve("CWT-" + testName).toFile())) {
                printHeader(pw, config);
                for (TestReport report : reports) {
                    pw.println(report.getClientWorkTime());
                }
            }
            try (PrintWriter pw = new PrintWriter(RESULTS_DIR.resolve("RQT-" + testName).toFile())) {
                printHeader(pw, config);
                for (TestReport report : reports) {
                    pw.println(report.getRequestHandleTime());
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Failed to save report", e);
        }
    }

    private static void printHeader(PrintWriter pw, TestConfig config) {
        pw.println(config.getMode());
        pw.println(config.getNumRequests());
        pw.println(config.getParameterType());
        pw.println(config.getParamLower());
        pw.println(config.getParamUpper());
        pw.println(config.getParamStep());
        pw.println(config.getParam1());
        pw.println(config.getParam2());
    }

    private void handleRecoverableError(String msg, Throwable e) {
        log.error(msg, e);
        val cause = e.getCause();
        final JComponent[] message = new JComponent[]{
                new JLabel("Problem:"),
                new JLabel(msg),
                new JLabel("Message:"),
                new JLabel((cause != null ? cause + " " : "") + e.getMessage())
        };

        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private TestReport runTest(int numRequests, int numClients, ServerMode mode, int delay, int arraySize) {
        final Server server;
        try {
            server = ServerFactory.create(31001, mode);
        } catch (IOException e) {
            handleRecoverableError("Could not start server", e);
            return null;
        }
        final List<Client> clients = new ArrayList<>(numClients);
        IntStream.range(0, numClients).forEach(x -> clients.add(new Client("localhost", 31001)));
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
                } catch (ServerUnavailableException | InterruptedException e) {
                    throw new RuntimeException(e);
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
    }
}
