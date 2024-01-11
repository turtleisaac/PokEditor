package io.github.turtleisaac.pokeditor.gui.editors.data;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public abstract class DefaultDataEditor<G extends GenericFileData, E extends Enum<E>> extends JPanel
{
    private EditorDataModel<E> model;
    private int selectedIndex;

    public DefaultDataEditor(EditorDataModel<E> model)
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

    public void addNewEntry()
    {
        JOptionPane.showMessageDialog(this, "Rowan's words echoed...\n\"There's a time and place for everything but not now!\"", "This function has not been implemented yet", JOptionPane.WARNING_MESSAGE);
    }

    public void deleteCurrentEntry()
    {
        JOptionPane.showMessageDialog(this, "Rowan's words echoed...\n\"There's a time and place for everything but not now!\"", "This function has not been implemented yet", JOptionPane.WARNING_MESSAGE);
    }

    public abstract Class<G> getDataClass();

    public abstract Set<DefaultDataEditorPanel.DataEditorButtons> getEnabledToolbarButtons();

    public BytesDataContainer writeSelectedEntryForCopy()
    {
        if (getModel() instanceof FormatModel<?,?> formatModel)
            return formatModel.getData().get(selectedIndex).save();
        return null;
    }

    public void applyCopiedEntry(BytesDataContainer bytesDataContainer)
    {
        try {
            if (getModel() instanceof FormatModel<?,?> formatModel)
                formatModel.getData().get(selectedIndex).setData(bytesDataContainer);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while pasting the copied data.", "PokEditor", JOptionPane.ERROR_MESSAGE);
        }
    }
}
