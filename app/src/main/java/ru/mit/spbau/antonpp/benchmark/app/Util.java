package ru.mit.spbau.antonpp.benchmark.app;

import lombok.val;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class Util {

    public static final Path RESULTS_DIR = Paths.get("results");
    public static final String REPORT_PREFIX = "report-";
    private static final DirectoryStream.Filter<Path> REPORT_FILTER =
            file -> (Files.isDirectory(file) && file.getFileName().toString().startsWith(REPORT_PREFIX));

    public static final String METRICS[] = {"CST", "CWT", "RHT"};
    public static final String REPORT_CONFIG = "config.txt";

    private Util() {}

    public static String getDate(String name) throws IOException {
        try (Scanner scanner = new Scanner(RESULTS_DIR.resolve(name).resolve(REPORT_CONFIG))) {
            for (int i = 0; i < 8; ++i) {
                scanner.nextLine();
            }
            return scanner.nextLine();
        }
    }

    private static XYSeries loadSeries(Path path, List<Integer> xs) throws IOException {
        try (Scanner scanner = new Scanner(path)) {
            final XYSeries series = new XYSeries(path.getFileName().toString());

            for (final Integer x : xs) {
                double y = scanner.nextDouble();
                series.add((int)x, y);
            }

            return series;
        }
    }

    public static XYSeriesCollection loadReportDataset(String name, int metric) throws IOException {
        val config = getConfig(name);
        List<Integer> xs = new ArrayList<>();
        for (int x = config.getParamLower(); x < config.getParamUpper(); x += config.getParamStep()) {
            xs.add(x);
        }
        final Path testPath = RESULTS_DIR.resolve(name);
        final XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(loadSeries(testPath.resolve(METRICS[metric]), xs));
        return dataset;
    }

    public static List<String> listReports() throws IOException {
        if (Files.notExists(RESULTS_DIR)) {
            return Collections.emptyList();
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(RESULTS_DIR, REPORT_FILTER)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    public static void writeConfig(String name, TestConfig config) throws IOException {
        try (PrintWriter pw = new PrintWriter(RESULTS_DIR.resolve(name).resolve(REPORT_CONFIG).toFile())) {
            pw.println(config.getMode());
            pw.println(config.getNumRequests());
            pw.println(config.getParameterType());
            pw.println(config.getParamLower());
            pw.println(config.getParamUpper());
            pw.println(config.getParamStep());
            pw.println(config.getParam1());
            pw.println(config.getParam2());
            pw.println(new Date().toString());
        }
    }

    public static void writeReports(String name, List<TestReport> reports) throws IOException {
        final Path testPath = RESULTS_DIR.resolve(name);
        try (PrintWriter pw = new PrintWriter(testPath.resolve(METRICS[0]).toFile())) {
            for (TestReport report : reports) {
                pw.println(report.getClientServeTime());
            }
        }
        try (PrintWriter pw = new PrintWriter(testPath.resolve(METRICS[1]).toFile())) {
            for (TestReport report : reports) {
                pw.println(report.getClientWorkTime());
            }
        }
        try (PrintWriter pw = new PrintWriter(testPath.resolve(METRICS[2]).toFile())) {
            for (TestReport report : reports) {
                pw.println(report.getRequestHandleTime());
            }
        }
    }

    public static TestConfig getConfig(String name) throws IOException {
        try (Scanner scanner = new Scanner(RESULTS_DIR.resolve(name).resolve(REPORT_CONFIG))) {
            return TestConfig.builder()
                    .mode(ServerMode.parse(scanner.nextLine()))
                    .numRequests(Integer.valueOf(scanner.nextLine()))
                    .parameterType(ParameterType.parse(scanner.nextLine()))
                    .paramLower(Integer.valueOf(scanner.nextLine()))
                    .paramUpper(Integer.valueOf(scanner.nextLine()))
                    .paramStep(Integer.valueOf(scanner.nextLine()))
                    .param1(Integer.valueOf(scanner.nextLine()))
                    .param2(Integer.valueOf(scanner.nextLine()))
                    .build();
        }
    }
}
