package io.github.turtleisaac.pokeditor.gui;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.text.ParseException;

public class HexadecimalSpinner extends JSpinner
{
    public HexadecimalSpinner()
    {
        super();
        configureEditor();
    }

    public HexadecimalSpinner(SpinnerModel model) {
        super(model);
        configureEditor();
    }

    private void configureEditor()
    {
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
        JFormattedTextField tf = editor.getTextField();
        tf.setFormatterFactory(new HexFormatterFactory());
    }

    private static class HexFormatterFactory extends DefaultFormatterFactory
    {
        public JFormattedTextField.AbstractFormatter getDefaultFormatter() {
            return new HexFormatter();
        }
    }

    private static class HexFormatter extends DefaultFormatter
    {
        public Object stringToValue(String text) throws ParseException
        {
            try {
                if (text.startsWith("0x"))
                    return (int) Long.parseLong(text.substring(2), 16);
                return (int) Long.parseLong(text);
            } catch (NumberFormatException nfe) {
                throw new ParseException(text,0);
            }
        }

        public String valueToString(Object value)
        {
            return "0x" + Long.toHexString((Integer) value).toUpperCase();
        }
    }
}
