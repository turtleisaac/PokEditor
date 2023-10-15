package io.github.turtleisaac.pokeditor.gui_old;

import com.jidesoft.swing.ComboBoxSearchable;

import javax.swing.*;
import java.util.Arrays;

public class EditorComboBox extends JComboBox<ComboBoxItem>
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
}
