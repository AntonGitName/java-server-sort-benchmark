package ru.mit.spbau.antonpp.benchmark.app.ui;

import lombok.val;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.mit.spbau.antonpp.benchmark.app.ParameterType;
import ru.mit.spbau.antonpp.benchmark.app.TestConfig;
import ru.mit.spbau.antonpp.benchmark.server.ServerMode;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import static ru.mit.spbau.antonpp.benchmark.app.ui.Application.REPORTS;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class PlotDialog extends JDialog {

    private TestConfig config;

    private XYSeries loadSeries(Path path) throws IOException {
        try (Scanner scanner = new Scanner(path)) {
            config = TestConfig.builder()
                    .mode(ServerMode.parse(scanner.nextLine()))
                    .numRequests(Integer.valueOf(scanner.nextLine()))
                    .parameterType(ParameterType.parse(scanner.nextLine()))
                    .paramLower(Integer.valueOf(scanner.nextLine()))
                    .paramUpper(Integer.valueOf(scanner.nextLine()))
                    .paramStep(Integer.valueOf(scanner.nextLine()))
                    .param1(Integer.valueOf(scanner.nextLine()))
                    .param2(Integer.valueOf(scanner.nextLine()))
                    .build();

            final XYSeries series = new XYSeries(path.getFileName().toString());

            for (int x = config.getParamLower(); x < config.getParamUpper(); x += config.getParamStep()) {
                double y = scanner.nextDouble();
                series.add(x, y);
            }

            return series;
        }
    }

    public PlotDialog(JFrame frame, Path path) {
        super(frame);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        try {

            final XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(loadSeries(path.resolve(REPORTS[0])));
            dataset.addSeries(loadSeries(path.resolve(REPORTS[1])));
            dataset.addSeries(loadSeries(path.resolve(REPORTS[2])));

            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, Color.GREEN);
            renderer.setSeriesPaint(2, Color.YELLOW);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    path.getFileName().toString(),
                    config.getParameterType().toString(),
                    "time, ms",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false);

            container.add(new ChartPanel(chart));
            container.add(new JLabel(String.format("<html>%s</html>", config.toString())));
            add(container);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load data series", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}
