package io.github.turtleisaac.pokeditor.gui.sheets.tables.editors;

import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;

import javax.swing.*;

public class EditorComboBoxEditor extends DefaultCellEditor
{
    public EditorComboBoxEditor(String[] items)
    {
        super(new EditorComboBox(items));
    }
}
