package ru.mit.spbau.antonpp.benchmark.app.panels;

import javax.swing.table.AbstractTableModel;

/**
 * @author antonpp
 * @since 25/12/2016
 */
public class ResultTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Configuration", "Parameter", "Requests", "Plots"};

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
