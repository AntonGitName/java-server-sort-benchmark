package ru.mit.spbau.antonpp.benchmark.app.ui.panels;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.mit.spbau.antonpp.benchmark.app.Util;
import ru.mit.spbau.antonpp.benchmark.app.ui.PlotDialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author antonpp
 * @since 25/12/2016
 */
@Slf4j
public class ResultTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Test id", "date", "Request Handle Time", "AbstractClient Serve Time", "AbstractClient Work Time"};

    private final List<Row> rows = new ArrayList<>();
    private final JFrame rootFrame;

    public ResultTableModel(List<String> names, JFrame rootFrame) {
        this.rootFrame = rootFrame;
        resetRows(names);
    }

    public void resetRows(List<String> names) {
        rows.clear();
        for (val name : names) {
            rows.add(new Row(name));
        }
        rows.sort(Comparator.comparing(x -> x.date));
        fireTableDataChanged();
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
                return rows.get(rowIndex).date;
            case 2:
                return rows.get(rowIndex).plotRht;
            case 3:
                return rows.get(rowIndex).plotCst;
            case 4:
                return rows.get(rowIndex).plotCvt;
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 2:
            case 3:
            case 4:
                return JButton.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private final class Row {
        private final String name;
        private final String date;
        private final JButton plotCvt;
        private final JButton plotCst;
        private final JButton plotRht;

        private Row(String name) {
            String tmp;
            try {
                tmp = Util.getDate(name);
            } catch (IOException e) {
                tmp = "not specified";
                log.warn("Could not retrieve test date");
            }
            date = tmp;
            this.name = name.substring(Util.REPORT_PREFIX.length());
            plotCst = createButton(name, 0);
            plotCvt = createButton(name, 1);
            plotRht = createButton(name, 2);
        }

        private JButton createButton(String name, int metric) {
            val button = new JButton("show!");
            button.addActionListener(e -> {
                PlotDialog plot = new PlotDialog(rootFrame, name, metric);
                plot.setSize(640, 480);
                plot.setVisible(true);
            });
            button.setEnabled(true);
            return button;
        }
    }
}
