package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

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
        if (val == 0)
            return 0;

        return (1 << val - 1);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        int val = (Integer) value;
        if (val == 0)
            comboBox.setSelectedIndex(0);
        else {
            val = (int) (Math.log(val) / Math.log(2)) + 1;
            comboBox.setSelectedIndex(val);
        }

        return comboBox;
    }
}
