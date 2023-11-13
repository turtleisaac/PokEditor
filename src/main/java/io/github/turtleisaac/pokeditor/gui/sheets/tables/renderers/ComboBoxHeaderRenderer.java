package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import io.github.turtleisaac.pokeditor.formats.personal.PersonalParser;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.ComboBoxCellEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ComboBoxHeaderRenderer extends ComboBoxCellEditor implements TableCellRenderer
{
    public ComboBoxHeaderRenderer(String[] items)
    {
        super(items);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (isSelected)
        {
            System.out.println("selected");
        }
        if (hasFocus)
        {
            System.out.println("focused");
        }
        System.out.println("---------");
        value = PersonalParser.tmMoveIdNumbers[column];
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
