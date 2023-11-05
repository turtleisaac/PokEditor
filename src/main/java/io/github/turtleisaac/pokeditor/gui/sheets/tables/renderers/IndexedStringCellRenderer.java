package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class IndexedStringCellRenderer extends DefaultTableCellRenderer
{
    String[] items;

    public IndexedStringCellRenderer(String[] items)
    {
        this.items = items;
    }

    public void setItems(String[] items)
    {
        this.items = items;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null)
        {
            if (value instanceof Integer val)
            {
                if (val < items.length)
                {
                    this.setText(items[val]);
                }
            }
        }

        if (isSelected || table.getSelectedRow() == row) {
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(getForeground());
            if (row % 2 == 0)
                setBackground(table.getBackground());
            else
                setBackground(new Color(248, 221, 231));
        }

        return this;
    }

    public static class ColoredIndexedStringCellRenderer extends IndexedStringCellRenderer
    {
        private final Color[] colors;

        public ColoredIndexedStringCellRenderer(String[] items, Color[] colors)
        {
            super(items);
            this.colors = colors;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//            Border border = getBorder();
            if (!isSelected && value != null)
            {
                if (value instanceof Integer)
                {
                    this.setBackground(colors[(int) value]); // always in bounds because of earlier check
                    this.setForeground(Color.black);
//                    setBorder(border);
                }
            }
            return this;
        }
    }
}
