package io.github.turtleisaac.variabletracker.gui.cells;

import com.formdev.flatlaf.ui.FlatSpinnerUI;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class NumberCellEditor extends AbstractCellEditor implements TableCellEditor
{
    private final JSpinner spinner;

    public NumberCellEditor(boolean hex)
    {
        if (hex)
            spinner = new HexadecimalSpinner();
        else
            spinner = new JSpinner();

        spinner.setUI(new FlatSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }

            protected Component createPreviousButton() {
                return null;
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        spinner.setValue(value);
        if (table.getValueAt(row, 0) instanceof Integer val)
        {
            spinner.setEnabled(!((val >= 0x4000 && val <= 0x401F) || val >= 0x8000));
        }
        return spinner;
    }

    @Override
    public Object getCellEditorValue()
    {
        return spinner.getValue();
    }
}
