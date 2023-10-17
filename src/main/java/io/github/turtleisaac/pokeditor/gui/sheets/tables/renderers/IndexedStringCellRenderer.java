package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class IndexedStringCellRenderer extends DefaultTableCellRenderer
{
    private String[] items;

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

        if (value != null) {
            if (value instanceof Integer)
                if ((Integer) value < items.length) {
                    int num = (int) value;
                    this.setText(items[num]);
                }
        }

        if (isSelected) {
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
            if ((column == 6 || column == 7) && !isSelected && value != null)
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
