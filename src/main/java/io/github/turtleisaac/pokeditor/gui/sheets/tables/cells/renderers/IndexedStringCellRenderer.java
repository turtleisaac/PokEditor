package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers;

import javax.swing.*;
import java.awt.*;

public class IndexedStringCellRenderer extends DefaultSheetCellRenderer
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
            else if (value instanceof String s)
            {
                int val = Integer.parseInt(s);
                if (val < items.length)
                {
                    this.setText(items[val]);
                }
            }
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
//                    this.setForeground(Color.black);
//                    setBorder(border);
                }
            }
            return this;
        }
    }
}
