package io.github.turtleisaac.pokeditor.gui;

import com.jidesoft.swing.ComboBoxSearchable;

import javax.swing.*;
import java.util.Arrays;

public class EditorComboBox extends JComboBox<EditorComboBox.ComboBoxItem>
{
    private final ComboBoxSearchable searchable;

    public EditorComboBox()
    {
        super();
        searchable= new ComboBoxSearchable(this);
    }

    public EditorComboBox(String[] items)
    {
        super(Arrays.stream(items).map(ComboBoxItem::new).toArray(ComboBoxItem[]::new));
        searchable= new ComboBoxSearchable(this);
    }

    public EditorComboBox(ComboBoxItem[] model)
    {
        super(model);
        searchable= new ComboBoxSearchable(this);
    }

    public EditorComboBox(ComboBoxModel<ComboBoxItem> model)
    {
        super(model);
        searchable= new ComboBoxSearchable(this);
    }

    public static class ComboBoxItem
    {
        private String str;

        public ComboBoxItem(String str)
        {
            this.str= str;
        }

        public ComboBoxItem(int num)
        {
            str= "" + num;
        }

        public void setName(String str) {this.str= str;}

        @Override
        public String toString()
        {
            return str;
        }
    }
}
