package ru.mit.spbau.antonpp.benchmark.app.ui;

import lombok.val;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class ButtonRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        val panel = (JButton) value;
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(table.getBackground());
        }
        return panel;
    }
}
