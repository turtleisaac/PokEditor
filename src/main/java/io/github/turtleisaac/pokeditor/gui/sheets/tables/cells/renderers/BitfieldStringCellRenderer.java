package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers;

import javax.swing.*;
import java.awt.*;

public class BitfieldStringCellRenderer extends IndexedStringCellRenderer
{
    public BitfieldStringCellRenderer(String[] items)
    {
        super(items);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null) {
            if (value instanceof Integer val)
            {
                if (val == 0) {
                    setText(items[0]);
                    return this;
                }

                val = (int) (Math.log(val) / Math.log(2) + 1);
                if (val < items.length) {
                    this.setText(items[val]);
                }
            }
        }

        return this;
    }
}
