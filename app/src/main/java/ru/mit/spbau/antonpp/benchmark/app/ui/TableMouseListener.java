package ru.mit.spbau.antonpp.benchmark.app.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Anton Mordberg
 * @since 11.01.17
 */
public class TableMouseListener extends MouseAdapter {
    private final JTable table;

    public TableMouseListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // get the coloum of the button
        int row    = e.getY()/table.getRowHeight(); //get the row of the button

                    /*Checking the row or column is valid or not*/
        if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
            Object value = table.getValueAt(row, column);
            if (value instanceof JButton) {
                    /*perform a click event*/
                ((JButton)value).doClick();
            }
        }
    }
}
