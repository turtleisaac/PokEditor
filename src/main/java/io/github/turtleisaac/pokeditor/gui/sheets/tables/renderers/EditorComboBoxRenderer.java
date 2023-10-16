package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class EditorComboBoxRenderer implements TableCellRenderer
{
    private final String[] items;

    public EditorComboBoxRenderer(String[] items)
    {
        this.items = items;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setShowGrid(true);

        EditorComboBox comboBox = new EditorComboBox(items);

        if (isSelected)
        {
            comboBox.setForeground(table.getSelectionForeground());
            comboBox.setBackground(table.getSelectionBackground());
        }
        else
        {
            comboBox.setForeground(table.getForeground());
            comboBox.setBackground(table.getBackground());
        }

        if (value != null) {
            if (value instanceof Integer)
                if ((Integer) value < comboBox.getItemCount())
                    comboBox.setSelectedIndex((Integer) value);
        }

        return comboBox;
    }
}
