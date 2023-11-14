package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers;

import javax.swing.*;
import java.awt.*;

public class CheckBoxRenderer extends DefaultSheetCellRenderer
{
    JPanel panel;
    JCheckBox checkBox;

    public CheckBoxRenderer()
    {
        super();
        panel = new JPanel();
        checkBox = new JCheckBox();
        panel.add(checkBox);
//        checkBox.setVisible(true);
//        add(panel);
//        add(checkBox);
//        add(checkBox);
//        add(new JButton());
        setPreferredSize(getPreferredSize());
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        checkBox.setSelected((Boolean) value);
        return panel;
    }


}
