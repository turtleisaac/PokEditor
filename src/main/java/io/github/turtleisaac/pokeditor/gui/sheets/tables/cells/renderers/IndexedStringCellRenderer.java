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
                if (val >= 0 && val < items.length)
                {
                    this.setText(items[val]);
                }
            }
            else if (value instanceof String s)
            {
                // a cell can hold text which is not a number at all - an empty cell, a partial
                // edit, a bad paste. this runs on the paint path, so an unguarded parse would
                // take the whole sheet down over one cell rather than just showing that cell oddly
                try
                {
                    int val = Integer.parseInt(s.trim());
                    if (val >= 0 && val < items.length)
                    {
                        this.setText(items[val]);
                    }
                }
                catch (NumberFormatException ignored)
                {
                    // leave the raw value the superclass already set - showing the user what is
                    // actually stored is more use than showing them nothing
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
                if (value instanceof Integer val)
                {
                    // NOTE: the superclass checks against items.length, but this array is a
                    // separate (and shorter) list of type colors, so it needs its own check -
                    // an out of range type value used to make the sheet permanently unpaintable
                    if (val < 0 || val >= colors.length)
                        return this;

                    this.setBackground(colors[val]);
//                    this.setForeground(Color.black);
//                    setBorder(border);
                }
            }
            return this;
        }
    }
}
