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
        comboBox.setSelectedIndex((Integer) value);
        return comboBox;
    }
}
