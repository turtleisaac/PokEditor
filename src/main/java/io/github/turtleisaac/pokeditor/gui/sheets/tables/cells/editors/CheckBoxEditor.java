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
        // see CheckBoxRenderer: a bare cast throws on null or on any non-Boolean, which would
        // mean a cell the renderer can display but the user cannot open
        checkBox.setSelected(value instanceof Boolean b && b);
        return panel;
    }

    @Override
    public Object getCellEditorValue()
    {
        return checkBox.isSelected();
    }
}
