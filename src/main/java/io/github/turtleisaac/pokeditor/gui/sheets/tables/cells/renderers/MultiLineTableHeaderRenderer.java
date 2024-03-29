package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MultiLineTableHeaderRenderer extends JTextArea implements TableCellRenderer
{
    public MultiLineTableHeaderRenderer()
    {
        setEditable(false);
        setLineWrap(true);
        setOpaque(false);
        setFocusable(false);
        setWrapStyleWord(true);
        LookAndFeel.installBorder(this, "TableHeader.cellBorder");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        int width = table.getColumnModel().getColumn(column).getWidth();
        setText(String.valueOf(value));
        setSize(width, getPreferredSize().height);
        if (table.getSelectedColumn() == column)
        {
            setForeground(UIManager.getColor("Component.custom.borderColor"));
        }
        else {
            setForeground(UIManager.getColor("TableHeader.foreground"));
        }
        return this;
    }
}