package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.CheckBoxRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.table;
import static org.assertj.core.api.Assertions.assertThat;

class CheckBoxEditorTest
{
    private final JTable table = table();

    @Test
    @DisplayName("true and false survive the render -> edit -> value round trip unchanged")
    void booleanRoundTrip()
    {
        // What the sheet paints, what the editor opens with, and what the editor hands back must
        // all be the same flag. If they disagree, opening a cell and clicking nothing still
        // flips the stored value.
        for (boolean stored : new boolean[] {true, false})
        {
            CheckBoxRenderer renderer = new CheckBoxRenderer();
            CheckBoxEditor editor = new CheckBoxEditor();

            boolean painted = checkBoxIn(renderer.getTableCellRendererComponent(table, stored, false, false, 1, 0)).isSelected();
            assertThat(painted).as("painted state for %s", stored).isEqualTo(stored);

            editor.getTableCellEditorComponent(table, stored, false, 1, 0);
            assertThat(editor.getCellEditorValue()).as("value produced with no user interaction for %s", stored).isEqualTo(stored);
        }
    }

    @Test
    @DisplayName("the value handed back is the state of the checkbox the user actually clicked")
    void userTogglePropagates()
    {
        // Derived from what an editor IS: it must report the widget's state, not the state it
        // was opened with.
        CheckBoxEditor editor = new CheckBoxEditor();
        JCheckBox box = checkBoxIn(editor.getTableCellEditorComponent(table, false, false, 1, 0));

        box.setSelected(true);
        assertThat(editor.getCellEditorValue()).isEqualTo(true);

        box.setSelected(false);
        assertThat(editor.getCellEditorValue()).isEqualTo(false);
    }

    @Test
    @DisplayName("opening a checkbox cell on a non-boolean value does not throw")
    void nonBooleanValueDoesNotThrow()
    {
        // A checkbox column can be handed a null or a pasted string. Throwing here escapes onto
        // the EDT when the user double-clicks the cell, leaving a cell that can never be edited.
        CheckBoxEditor editor = new CheckBoxEditor();

        java.util.List<String> failures = CellsTestSupport.failuresOver(
                java.util.Arrays.asList(null, "true", "", 0, 1, new Object()),
                value -> editor.getTableCellEditorComponent(table, value, false, 1, 0));

        assertThat(failures).as("values this checkbox editor cannot open on").isEmpty();
    }

    private static JCheckBox checkBoxIn(Component c)
    {
        if (c instanceof JCheckBox box)
            return box;
        if (c instanceof Container container)
        {
            for (Component child : container.getComponents())
            {
                JCheckBox found = checkBoxIn(child);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
}
