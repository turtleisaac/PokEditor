package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class EditorComboBoxRenderer implements TableCellRenderer
{
    private final String[] items;
    private final Color[] colors;

    public EditorComboBoxRenderer(String[] items, Color[] colors)
    {
        this.items = items;
        this.colors = colors;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setShowGrid(true);

        EditorComboBox comboBox = new EditorComboBox(items);
        JLabel label = new JLabel();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(label);
        if (isSelected)
        {
            label.setForeground(table.getForeground());
            label.setBackground(table.getSelectionBackground());
            panel.setBackground(table.getSelectionBackground());
        }
        else
        {
            label.setForeground(table.getForeground());
            label.setBackground(table.getBackground());
        }

        if (value != null) {
            if (value instanceof Integer)
                if ((Integer) value < items.length) {
//                    comboBox.setSelectedIndex((Integer) value);
                    int num = (int) value;
                    label.setText(items[num]);
                    if (!isSelected && num < colors.length)
                        panel.setBackground(colors[num]);
                }

        }

        panel.setBorder(BorderFactory.createLineBorder(table.getGridColor()));

        return panel;
    }
}
