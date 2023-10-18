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
        setForeground(Color.black);
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else if (table.getSelectedRow() == row) {
            setBackground(table.getSelectionBackground());
        }
        else {
            if (row % 2 == 0)
                setBackground(table.getBackground());
            else
                setBackground(new Color(248, 221, 231));
        }
        validate();
        repaint();
        setSelected((Boolean) value);
        return this;
    }
}
