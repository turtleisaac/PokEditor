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

    private DefaultDataEditorPanel<G, E> panel;

    public DefaultDataEditor(EditorDataModel<E> model)
    {
        this.model = model;
        selectedIndex = -1;
    }

    void setPanel(DefaultDataEditorPanel<G, E> panel)
    {
        this.panel = panel;
    }

    /**
     * @return the panel hosting this editor (which owns the entry selector), or null if this
     * editor has not been added to one
     */
    protected DefaultDataEditorPanel<G, E> getPanel()
    {
        return panel;
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
        // -1 is the "nothing selected yet" sentinel, and the toolbar's copy button is live from
        // the moment the editor opens. Feeding it to get() raised IndexOutOfBoundsException at
        // the user; "there is nothing to copy" is the same answer this method already gives for
        // a model it cannot read.
        if (!hasSelection())
            return null;

        if (getModel() instanceof FormatModel<?,?> formatModel)
            return formatModel.getData().get(selectedIndex).save();
        return null;
    }

    /**
     * @return whether an entry is selected and is within the model's bounds
     */
    private boolean hasSelection()
    {
        return selectedIndex >= 0 && getModel() != null && selectedIndex < getModel().getEntryCount();
    }

    public void applyCopiedEntry(BytesDataContainer bytesDataContainer)
    {
        // see writeSelectedEntryForCopy: with nothing selected there is nowhere to paste, and
        // get(-1) threw from inside a catch that then tried to raise a dialog about it
        if (!hasSelection() || bytesDataContainer == null)
            return;

        try {
            if (getModel() instanceof FormatModel<?,?> formatModel)
                formatModel.getData().get(selectedIndex).setData(bytesDataContainer);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while pasting the copied data.", "PokEditor", JOptionPane.ERROR_MESSAGE);
        }
    }
}
