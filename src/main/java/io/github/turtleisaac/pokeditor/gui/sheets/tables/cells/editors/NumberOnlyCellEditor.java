package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class NumberOnlyCellEditor extends AbstractCellEditor implements TableCellEditor
{
    public static final int DEFAULT_MINIMUM = 0;
    public static final int DEFAULT_MAXIMUM = 255;

    private final JTextField textField;
    private final int minimum;
    private final int maximum;
    private Object lastValue;

    public NumberOnlyCellEditor()
    {
        this(DEFAULT_MINIMUM, DEFAULT_MAXIMUM);
    }

    /**
     * @param minimum the smallest value this column can legally hold (inclusive)
     * @param maximum the largest value this column can legally hold (inclusive)
     */
    public NumberOnlyCellEditor(int minimum, int maximum)
    {
        this.minimum = minimum;
        this.maximum = maximum;

        textField = new JTextField();
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
            {
                fb.insertString(offset, sanitize(offset, string), attr);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
                    throws BadLocationException {
                fb.replace(off, len, sanitize(off, str), attr);
            }
        });
    }

    /**
     * @return whether the column this editor serves is allowed to hold negative values
     */
    private boolean isSigned()
    {
        return minimum < 0;
    }

    /**
     * Strips out every character which is not a digit, but permits a single leading minus
     * sign when the column this editor serves is signed. A minus sign anywhere but at
     * offset 0 is rejected.
     */
    private String sanitize(int offset, String string)
    {
        if (string == null)
            return "";

        String digits = string.replaceAll("\\D++", "");

        if (isSigned() && offset == 0 && string.startsWith("-"))
            return "-" + digits;

        return digits;
    }

    public int getMinimum()
    {
        return minimum;
    }

    public int getMaximum()
    {
        return maximum;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        lastValue = String.valueOf(value);
        if (value instanceof String)
            textField.setText((String) value);
        else if (value instanceof Integer)
            textField.setText(((Integer) value).toString());
        else
            textField.setText(""); // a blank cell - do not leave the previous cell's text behind
        return textField;
    }

    @Override
    public boolean stopCellEditing()
    {
        String text = textField.getText().trim();

        int value;
        try {
            value = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            reportInvalidValue(text);
            return false;
        }

        if (value < minimum || value > maximum)
        {
            reportInvalidValue(text);
            return false;
        }

        return super.stopCellEditing();
    }

    private void reportInvalidValue(String text)
    {
        JOptionPane.showMessageDialog(textField,
                String.format("\"%s\" is not a valid value for this cell.\nThis cell accepts whole numbers between %d and %d.", text, minimum, maximum),
                "Invalid Value",
                JOptionPane.ERROR_MESSAGE);
        textField.requestFocusInWindow();
    }

    @Override
    public Object getCellEditorValue()
    {
        String text = textField.getText().trim();
        try {
            int value = Integer.parseInt(text);
            if (value >= minimum && value <= maximum)
                return text;
        }
        catch (NumberFormatException ignored) {}

        return lastValue;
    }
}
