package ru.mit.spbau.antonpp.benchmark.app.ui;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class ResultCellEditor implements TableCellEditor {
    public ResultCellEditor() {
        super();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return (Component) value;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        return false;
    }

    @Override
    public void cancelCellEditing() {

    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {

    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {

    }
}
