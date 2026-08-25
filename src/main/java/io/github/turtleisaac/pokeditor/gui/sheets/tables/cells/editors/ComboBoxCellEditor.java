package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors;

import io.github.turtleisaac.pokeditor.gui.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor
{

    EditorComboBox comboBox;

    /**
     * The value the cell held when editing began.
     * <p>
     * A combo box reports -1 when nothing is selected, which is what happens when the user
     * types a name that does not match an entry exactly and then commits - type-to-search
     * leaves the selection empty rather than guessing. Reporting -1 as the new value made the
     * sheet reject the edit with an error, so typing a move name failed and the user had to
     * find it in the list by hand. Handing back the original value instead means an edit that
     * selected nothing simply does nothing, which is how the numeric editor already behaves.
     */
    private Object lastValue;

    public ComboBoxCellEditor(String[] items)
    {
        comboBox = new EditorComboBox(items);
    }

    public void setItems(String[] items)
    {
        comboBox = new EditorComboBox(items);
    }

    /**
     * @return the value the cell held when editing began, for a subclass that needs to leave it
     * unchanged
     */
    protected Object getLastValue()
    {
        return lastValue;
    }

    @Override
    public Object getCellEditorValue()
    {
        int selected = comboBox.getSelectedIndex();
        return selected >= 0 ? selected : lastValue;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        // the matching renderer ignores an out of range index and paints the cell harmlessly, so
        // without the same tolerance here a cell can be displayed but not opened - and a value
        // the sheet shows as wrong becomes one the user has no way to correct. clamp to "no
        // selection" instead of throwing on the EDT.
        int idx = (value instanceof Integer i) ? i : -1;
        lastValue = value;
        comboBox.setSelectedIndex(idx >= 0 && idx < comboBox.getItemCount() ? idx : -1);
        return comboBox;
    }
}
