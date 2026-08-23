package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors;

import io.github.turtleisaac.pokeditor.gui.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor
{

    EditorComboBox comboBox;

    public ComboBoxCellEditor(String[] items)
    {
        comboBox = new EditorComboBox(items);
    }

    public void setItems(String[] items)
    {
        comboBox = new EditorComboBox(items);
    }

    @Override
    public Object getCellEditorValue()
    {
        return comboBox.getSelectedIndex();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        // the matching renderer ignores an out of range index and paints the cell harmlessly, so
        // without the same tolerance here a cell can be displayed but not opened - and a value
        // the sheet shows as wrong becomes one the user has no way to correct. clamp to "no
        // selection" instead of throwing on the EDT.
        int idx = (value instanceof Integer i) ? i : -1;
        comboBox.setSelectedIndex(idx >= 0 && idx < comboBox.getItemCount() ? idx : -1);
        return comboBox;
    }
}
