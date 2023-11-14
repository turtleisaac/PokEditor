package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class CheckBoxEditor extends AbstractCellEditor implements TableCellEditor
{
    JPanel panel = new JPanel();
    JCheckBox checkBox;

    public CheckBoxEditor()
    {
        super();
        checkBox = new JCheckBox();
        panel.add(checkBox);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        checkBox.setSelected((Boolean) value);
        return panel;
    }

    @Override
    public Object getCellEditorValue()
    {
        return checkBox.isSelected();
    }
}
