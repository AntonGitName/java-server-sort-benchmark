package ru.mit.spbau.antonpp.benchmark.app.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import ru.mit.spbau.antonpp.benchmark.app.TestConfig;
import ru.mit.spbau.antonpp.benchmark.app.Util;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class PlotDialog extends JDialog {


    public PlotDialog(JFrame frame, String name, int metric) {
        super(frame);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        try {

            final XYSeriesCollection dataset = Util.loadReportDataset(name, metric);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            final Color color;
            switch (metric) {
                case 0:
                    color = Color.red;
                    break;
                case 1:
                    color = Color.green;
                    break;
                case 2:
                    color = Color.blue;
                    break;
                default:
                    color = Color.black;
            }
            renderer.setSeriesPaint(0, color);


            TestConfig config = Util.getConfig(name);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    name,
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
