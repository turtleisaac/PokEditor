package io.github.turtleisaac.variabletracker.gui.cells;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HexadecimalCellRenderer extends DefaultTableCellRenderer
{
    public HexadecimalCellRenderer()
    {
        System.currentTimeMillis();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        int num;
        if (value instanceof Integer integer)
            num = integer;
        else if (value instanceof String str)
        {
            if (str.startsWith("0x"))
                num = Integer.parseInt(str.substring(2), 16);
            else
                num = Integer.parseInt(str);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred with the data contained within");
            throw new RuntimeException("Invalid data type provided");
        }

        label.setText("0x" + Integer.toHexString(num).toUpperCase());

        if (table.getValueAt(row, 0) instanceof Integer val)
        {
            setEnabled(!((val >= 0x4000 && val <= 0x401F) || val >= 0x8000));
        }

        return this;
    }
}
