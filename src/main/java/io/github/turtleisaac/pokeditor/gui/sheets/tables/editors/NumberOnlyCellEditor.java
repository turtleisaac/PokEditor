package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class NumberOnlyCellEditor extends AbstractCellEditor implements TableCellEditor
{
    private final JTextField textField;

    public NumberOnlyCellEditor()
    {
        textField = new JTextField();
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
            {
                fb.insertString(offset, string.replaceAll("\\D++", ""), attr);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
                    throws BadLocationException {
                fb.replace(off, len, str.replaceAll("\\D++", ""), attr);
            }
        });

//        this.spinner = new JSpinner();
//        SpinnerNumberModel model = new SpinnerNumberModel();
//        model.setMinimum(0);
//        model.setMaximum(255);
//        spinner.setModel(model);
//
//        Dimension d = spinner.getPreferredSize();
//        d.width = 30;
//        spinner.setUI(new FlatSpinnerUI() {
//            protected Component createNextButton() {
//                return null;
//            }
//
//            protected Component createPreviousButton() {
//                return null;
//            }
//        });
//        spinner.setPreferredSize(d);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (value instanceof String)
            textField.setText((String) value);
        else if (value instanceof Integer)
            textField.setText(((Integer) value).toString());
        return textField;
    }

    @Override
    public Object getCellEditorValue()
    {
        return textField.getText();
    }
}
