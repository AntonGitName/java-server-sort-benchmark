package ru.mit.spbau.antonpp.benchmark.app.ui.panels;

import lombok.val;
import ru.mit.spbau.antonpp.benchmark.app.ui.PlotDialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.mit.spbau.antonpp.benchmark.app.ui.Application.REPORT_PREFIX;

/**
 * @author antonpp
 * @since 25/12/2016
 */
public class ResultTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Test id", "Request Handle Time, Client Serve Time, Client Work Time"};

    private final List<Row> rows = new ArrayList<>();
    private final JFrame rootFrame;

    public ResultTableModel(List<Path> names, JFrame rootFrame) {
        this.rootFrame = rootFrame;
        resetRows(names);
    }

    public void resetRows(List<Path> names) {
        rows.clear();
        for (val name : names) {
            rows.add(new Row(name));
        }
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rows.get(rowIndex).name;
            case 1:
                return rows.get(rowIndex).plotButton;
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 1:
                return JButton.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        return super.isCellEditable(rowIndex, columnIndex);
    }

    private final class Row {
        private final String name;
        private final JButton plotButton;

        private Row(Path path) {
            this.name = path.getFileName().toString().substring(REPORT_PREFIX.length());
            plotButton = new JButton("show!");
            plotButton.addActionListener(e -> {
                PlotDialog plot = new PlotDialog(rootFrame, path);
                plot.setSize(640, 480);
                plot.setVisible(true);
            });
            PlotDialog plot = new PlotDialog(rootFrame, path);
            plot.setVisible(true);
            plotButton.setEnabled(true);
        }
    }
}
