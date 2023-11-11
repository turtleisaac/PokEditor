package io.github.turtleisaac.pokeditor.gui.editors;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class DefaultEditor<G extends GenericFileData, E extends Enum<E>> extends JPanel
{
    private EditorDataModel<E> model;
    private int selectedIndex;

    public DefaultEditor(EditorDataModel<E> model)
    {
        this.model = model;
        selectedIndex = -1;
    }

    public EditorDataModel<E> getModel()
    {
        return model;
    }

    public void setModel(EditorDataModel<E> model)
    {
        this.model = model;
    }

    public void selectedIndexedChanged(int idx, ActionEvent e)
    {
        selectedIndex = idx;
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    public abstract Class<G> getDataClass();
}
