package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class EditorComboBoxEditor extends AbstractCellEditor implements TableCellEditor
{
//    EditorComboBox[] comboBoxes;
//    int lastRow = 0;

    EditorComboBox comboBox;

    public EditorComboBoxEditor(String[] items, int numRows)
    {
//        comboBoxes = new EditorComboBox[numRows];
//        for (int i = 0; i < numRows; i++)
//            comboBoxes[i] = new EditorComboBox(items);

        comboBox = new EditorComboBox(items);
    }

    @Override
    public Object getCellEditorValue()
    {
//        return comboBoxes[lastRow].getSelectedIndex();
        return comboBox.getSelectedIndex();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
//        lastRow = row;
//        comboBoxes[row].setSelectedIndex((Integer) value);
//        return comboBoxes[row];

        comboBox.setSelectedIndex((Integer) value);
        return comboBox;
    }
}
