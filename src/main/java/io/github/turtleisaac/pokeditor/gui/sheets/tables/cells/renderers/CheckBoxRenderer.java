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
        // a bare (Boolean) cast here is a paint-path landmine: null throws NPE and anything
        // else throws ClassCastException, either of which kills the whole sheet over one cell.
        // anything that is not a true Boolean simply reads as unticked.
        checkBox.setSelected(value instanceof Boolean b && b);
        return panel;
    }


}
