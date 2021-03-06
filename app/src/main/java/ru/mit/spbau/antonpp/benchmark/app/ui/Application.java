package ru.mit.spbau.antonpp.benchmark.app.ui;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.app.*;
import ru.mit.spbau.antonpp.benchmark.app.ui.panels.InfiniteProgressPanel;
import ru.mit.spbau.antonpp.benchmark.app.ui.panels.ResultTableModel;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ru.mit.spbau.antonpp.benchmark.app.ParameterType.NUMBER_CLIENTS;

/**
 * @author antonpp
 * @since 25/12/2016
 */
@Slf4j
public class Application extends JFrame {

        private InfiniteProgressPanel testProgress;
    private final ExecutorService testExecutor = Executors.newSingleThreadExecutor();
    private final ResultTableModel tableModel;

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
                testExecutor.shutdownNow();
            }
        });

        tableModel = new ResultTableModel(Collections.emptyList(), this);
        reloadTests();

        createUIElements();

        setVisible(true);
    }

    private void createUIElements() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        final JTable table = new JTable(tableModel);
        table.setDefaultRenderer(JButton.class, new ButtonRenderer());
        table.setRowHeight(table.getRowHeight() * 2);
        table.getColumn(tableModel.getColumnName(1)).setCellEditor(new ResultCellEditor());
        table.addMouseListener(new TableMouseListener(table));
//        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane);
        final JButton button = new JButton("Create new Test");
        panel.add(button);
        button.addActionListener(e -> onCreateTest());
        add(panel);
        testProgress = new InfiniteProgressPanel("Tests are running. Please wait...");
        setGlassPane(testProgress);
    }

    private void onCreateTest() {
        final JComboBox<ServerMode> comboBox = new JComboBox<>(ServerMode.values());
        final JComboBox<ParameterType> comboBox2 = new JComboBox<>(ParameterType.values());
        final JLabel label1 = new JLabel();
        final JLabel label2 = new JLabel();
        final JLabel label3 = new JLabel();
        final JLabel label4 = new JLabel();
        final JTextField param1 = new JTextField("10");
        final JTextField param2 = new JTextField("20");
        final JTextField param3 = new JTextField("1");
        final JTextField param4 = new JTextField("100");
        final JTextField step = new JTextField("1");

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
                        .parameterType(comboBox2.getItemAt(comboBox2.getSelectedIndex()))
                        .build();
                startTests(config);
            } catch (NumberFormatException e) {
                handleRecoverableError("Could not parse parameters", e);
            }
        }
    }

    private void startTests(TestConfig config) {
        final TestRunner testRunner = new TestRunner(config);

        testProgress.start();
        testExecutor.submit(() -> {
            try {
                final List<TestReport> reports = testRunner.run();
                saveReports(reports, config);
                SwingUtilities.invokeLater(this::onTestFinish);
            } catch (TestExecutionException e) {
                SwingUtilities.invokeLater(() -> onTestFail(e));
            }
        });
    }

    private void onTestFail(TestExecutionException e) {
        handleRecoverableError(e.getMessage(), e.getCause());
        testProgress.stop();
    }

    private void onTestFinish() {
        reloadTests();
        testProgress.stop();
    }

    private void reloadTests() {
        try {
          tableModel.resetRows(Util.listReports());
        } catch (IOException e) {
            handleRecoverableError("Could not load test reports", e);
        }
    }

    private void saveReports(List<TestReport> reports, TestConfig config) {
        try {
            if (!Files.exists(Util.RESULTS_DIR)) {
                Files.createDirectory(Util.RESULTS_DIR);
            }
            val testName = Util.REPORT_PREFIX + UUID.randomUUID();
            val testPath = Util.RESULTS_DIR.resolve(testName);

            Files.createDirectory(testPath);

            Util.writeConfig(testName, config);
            Util.writeReports(testName, reports);
        } catch (IOException e) {
            handleRecoverableError("Failed to save report", e);
        }
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
}
