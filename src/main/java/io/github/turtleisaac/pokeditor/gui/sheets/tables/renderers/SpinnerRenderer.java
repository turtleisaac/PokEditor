package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SpinnerRenderer extends JSpinner implements TableCellRenderer
{

    public SpinnerRenderer()
    {
        super(new SpinnerNumberModel(0, 0, 255, 1));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
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

        if(value instanceof Integer)
            setValue(value);
        else if(value instanceof String)
            setValue(Integer.valueOf((String) value));
        else
            setValue(0);
        return this;
    }
}
