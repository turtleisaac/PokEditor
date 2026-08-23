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
                    if (items.length > 0)
                        setText(items[0]);
                    return this;
                }

                int idx = highestSetBit(val) + 1;
                if (idx >= 0 && idx < items.length) {
                    this.setText(items[idx]);
                }
            }
        }

        return this;
    }

    /**
     * The index of the highest set bit of {@code val}, counting from zero, so entry
     * {@code n} of the name list is the flag {@code 1 << (n - 1)}.
     * <p>
     * This is integer arithmetic on purpose. The obvious {@code Math.log(val) / Math.log(2)}
     * is a floating point approximation of an exact quantity: it returns NaN for a negative
     * value (which then casts to 0, silently naming the wrong flag), and it is only as
     * accurate as the last bit of a double. The renderer and the editor also used to round it
     * at different points - {@code (int)(log + 1)} against {@code (int)log + 1} - so for a
     * sign-extended value the two disagreed about which flag the cell held, and merely opening
     * such a cell and closing it again rewrote it.
     *
     * @param val a non-zero bitfield value
     * @return the zero-based position of its most significant set bit
     */
    public static int highestSetBit(int val)
    {
        return 31 - Integer.numberOfLeadingZeros(val);
    }
}
