package io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class DefaultSheetCellRenderer extends DefaultTableCellRenderer
{
    public DefaultSheetCellRenderer()
    {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected || table.getSelectedRow() == row) {
            Border border = UIManager.getBorder("Table.focusCellHighlightBorder");
            setBorder(border);
            if (!isSelected)
            {
                setBackground(UIManager.getColor("Table.selectionInactiveBackground"));
            }
            else
            {
                setBackground(UIManager.getColor("Component.custom.borderColor"));
            }

        }
        else {
            if (row % 2 == 0)
                setBackground(table.getBackground());
            else
                setBackground(UIManager.getColor("TableHeader.pressedBackground"));
        }

//        if (isSelected || table.getSelectedRow() == row) {
//            setBackground(table.getSelectionBackground());
//        } else {
//            setForeground(getForeground());

//        }
//        setBorder(BorderFactory.createCompoundBorder());

        return this;
    }
}
