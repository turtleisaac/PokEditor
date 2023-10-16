package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

import com.formdev.flatlaf.ui.FlatSpinnerUI;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor
{
    private final JSpinner spinner;

    public SpinnerCellEditor()
    {
        this.spinner = new JSpinner();

        Dimension d = spinner.getPreferredSize();
        d.width = 30;
        spinner.setUI(new FlatSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }

            protected Component createPreviousButton() {
                return null;
            }
        });
        spinner.setPreferredSize(d);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        spinner.setValue(value);
        return spinner;
    }

    @Override
    public Object getCellEditorValue()
    {
        return spinner.getValue();
    }
}
