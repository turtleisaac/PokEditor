package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer
{

    public CheckBoxRenderer()
    {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setShowGrid(true);

        if (isSelected)
        {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else
        {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setSelected((Boolean) value);
        return this;
    }
}
