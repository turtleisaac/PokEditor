package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.BitfieldStringCellRenderer;

import javax.swing.*;
import java.awt.*;

public class BitfieldComboBoxEditor extends ComboBoxCellEditor
{
    public BitfieldComboBoxEditor(String[] items)
    {
        super(items);
    }

    @Override
    public Object getCellEditorValue()
    {
        int val = comboBox.getSelectedIndex();
        // -1 is "nothing selected", which happens when the cell holds a bit this column has no
        // name for. shifting by -2 would return a nonsense flag and write it into the file, so
        // report no flags set instead.
        if (val <= 0)
            return 0;

        return (1 << val - 1);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        // must agree with BitfieldStringCellRenderer exactly, and by sharing its arithmetic
        // rather than by restating it - the two used to round the same log expression at
        // different points, so a value the sheet painted as one flag opened as another and
        // closing the editor wrote that second flag back
        int val = (value instanceof Integer i) ? i : 0;
        if (val == 0)
        {
            comboBox.setSelectedIndex(comboBox.getItemCount() > 0 ? 0 : -1);
        }
        else
        {
            int idx = BitfieldStringCellRenderer.highestSetBit(val) + 1;
            // an undeclared bit has no entry to select; leaving the editor blank is honest,
            // where setSelectedIndex would throw on the EDT and make the cell unopenable
            comboBox.setSelectedIndex(idx >= 0 && idx < comboBox.getItemCount() ? idx : -1);
        }

        return comboBox;
    }
}
