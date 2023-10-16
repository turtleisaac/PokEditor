package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorComboBoxEditor extends AbstractCellEditor implements TableCellEditor
{

    EditorComboBox comboBox;

    public EditorComboBoxEditor(String[] items)
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
