package io.github.turtleisaac.pokeditor.gui.editors;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DefaultEditor<G extends GenericFileData, E extends Enum<E>> extends JPanel
{
    private EditorDataModel<E> model;

    public DefaultEditor(EditorDataModel<E> model)
    {
        this.model = model;
    }

    public EditorDataModel<E> getModel()
    {
        return model;
    }

    public void setModel(EditorDataModel<E> model)
    {
        this.model = model;
    }

    public void selectedIndexedChanged(int idx, ActionEvent e) {}
}
