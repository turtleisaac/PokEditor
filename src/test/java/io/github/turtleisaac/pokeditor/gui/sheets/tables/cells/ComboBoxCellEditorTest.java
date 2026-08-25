package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ComboBoxCellEditorTest
{
    private static final String[] ITEMS = names(21);

    private final JTable table = table();

    private JComboBox<?> comboOf(ComboBoxCellEditor editor, Object openOn)
    {
        return (JComboBox<?>) editor.getTableCellEditorComponent(table, openOn, false, 1, 0);
    }

    @Test
    @DisplayName("offers exactly the options it was constructed with, in order")
    void offersTheItemsItWasGiven()
    {
        // The dropdown is the user's entire view of the column's vocabulary. A missing or
        // reordered option silently shifts every index the user picks.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);
        JComboBox<?> combo = comboOf(editor, 0);

        assertThat(combo.getItemCount()).isEqualTo(ITEMS.length);
        for (int i = 0; i < ITEMS.length; i++)
            assertThat(displayedTextAt(combo, i)).as("option %d", i).isEqualTo(ITEMS[i]);
    }

    @Test
    @DisplayName("picking the option shown for index i produces exactly i")
    void pickingAnOptionProducesItsOwnIndex()
    {
        // Found by the text the user sees, not by the index the test already knows - that is the
        // only way this catches a dropdown whose visible order and stored order have diverged.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);
        JComboBox<?> combo = comboOf(editor, 0);

        for (int i = 0; i < ITEMS.length; i++)
        {
            selectByDisplayedText(combo, ITEMS[i]);
            assertThat(editor.getCellEditorValue()).as("value stored after picking '%s'", ITEMS[i]).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("what the renderer displays for a value is the option the editor stores that value for")
    void rendererAndEditorAgreeOnEveryOption()
    {
        // The deep one. If the two disagree the user picks what they see and the sheet stores
        // something else - the Evolutions method column writing "trade holding item" when the
        // user picked a species name.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);
        JComboBox<?> combo = comboOf(editor, 0);

        for (int i = 0; i < combo.getItemCount(); i++)
        {
            String shownInDropdown = displayedTextAt(combo, i);

            // the user picks the row that says this, and the sheet stores...
            selectByDisplayedText(combo, shownInDropdown);
            Object stored = editor.getCellEditorValue();

            // ...which the sheet then paints as...
            JLabel painted = (JLabel) renderer.getTableCellRendererComponent(table, stored, false, false, 1, 0);

            assertThat(painted.getText())
                    .as("cell text after picking option %d ('%s'), which stored %s", i, shownInDropdown, stored)
                    .isEqualTo(shownInDropdown);
        }
    }

    @Test
    @DisplayName("opening a cell whose stored value is out of range does not throw")
    void openingOnAnOutOfRangeValueDoesNotThrow()
    {
        // The editor half of the bug that made an out-of-range type unpaintable: the renderer now
        // survives such a value, but the user still has to be able to open the cell to fix it.
        // Throwing here escapes onto the EDT on a double click and the cell can never be repaired.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);

        List<Object> values = Arrays.asList(ITEMS.length, ITEMS.length + 5, 9999, -1, Integer.MAX_VALUE, null, "3", 3.5d);
        List<String> failures = failuresOver(values,
                value -> editor.getTableCellEditorComponent(table, value, false, 1, 0));

        assertThat(failures).as("stored values this editor cannot open a cell on").isEmpty();
    }

    @Test
    @DisplayName("opening a cell preselects the option that value already means")
    void openingPreselectsTheStoredValue()
    {
        // An editor that opens on the wrong row turns "double click, press escape" into a silent
        // edit, because the sheet takes the editor's value when editing stops.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);

        for (int i = 0; i < ITEMS.length; i++)
        {
            JComboBox<?> combo = comboOf(editor, i);
            assertThat(displayedTextAt(combo, combo.getSelectedIndex()))
                    .as("option preselected for stored value %d", i).isEqualTo(ITEMS[i]);
            assertThat(editor.getCellEditorValue())
                    .as("value produced by opening on %d and touching nothing", i).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("after setItems the round trip holds against the new list")
    void roundTripSurvivesSetItems()
    {
        // The sheet swaps text banks under a live table. If the editor and the renderer end up on
        // different lists, every pick in that column stores the wrong index from then on.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(ITEMS);
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);

        String[] replacement = {"alpha", "beta", "gamma", "delta"};
        editor.setItems(replacement);
        renderer.setItems(replacement);

        JComboBox<?> combo = comboOf(editor, 0);
        assertThat(combo.getItemCount()).as("option count after setItems").isEqualTo(replacement.length);

        for (int i = 0; i < replacement.length; i++)
        {
            selectByDisplayedText(combo, replacement[i]);
            Object stored = editor.getCellEditorValue();
            assertThat(stored).as("value stored after picking '%s'", replacement[i]).isEqualTo(i);

            JLabel painted = (JLabel) renderer.getTableCellRendererComponent(table, stored, false, false, 1, 0);
            assertThat(painted.getText()).as("cell text for stored value %s", stored).isEqualTo(replacement[i]);
        }
    }

    @Test
    @DisplayName("an edit that selects nothing leaves the cell as it was")
    void noSelectionKeepsTheOriginalValue()
    {
        // A combo box reports -1 when nothing is selected, and that is what type-to-search
        // leaves behind when the typed text matches no entry exactly. Reporting -1 as the new
        // value made the sheet reject the edit outright, so typing a move name produced an error
        // and the user had to find it in the list by hand. An edit that selected nothing should
        // do nothing - which is how the numeric editor has always behaved.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(new String[] {"Tackle", "Growl", "Ember"});
        editor.getTableCellEditorComponent(new javax.swing.JTable(), 2, false, 0, 0);

        ((javax.swing.JComboBox<?>) editor.getTableCellEditorComponent(
                new javax.swing.JTable(), 2, false, 0, 0)).setSelectedIndex(-1);

        assertThat(editor.getCellEditorValue())
                .as("nothing selected means the cell keeps what it had")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("a real selection is reported as the new value")
    void aSelectionIsReported()
    {
        // the other half: the no-op must not swallow a genuine edit
        ComboBoxCellEditor editor = new ComboBoxCellEditor(new String[] {"Tackle", "Growl", "Ember"});
        javax.swing.JComboBox<?> box = (javax.swing.JComboBox<?>) editor.getTableCellEditorComponent(
                new javax.swing.JTable(), 0, false, 0, 0);
        box.setSelectedIndex(2);

        assertThat(editor.getCellEditorValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("a value the column has no name for still opens, and closing changes nothing")
    void outOfRangeValueOpensAndIsPreserved()
    {
        // a hacked ROM can hold an index past the name list. The renderer paints it harmlessly,
        // so the editor must open too - and if the user closes without picking, the odd value
        // must survive rather than being replaced by -1 or by 0.
        ComboBoxCellEditor editor = new ComboBoxCellEditor(new String[] {"Tackle", "Growl"});

        assertThatCode(() -> editor.getTableCellEditorComponent(
                new javax.swing.JTable(), 99, false, 0, 0)).doesNotThrowAnyException();
        assertThat(editor.getCellEditorValue())
                .as("an unrecognised value is left alone, not overwritten")
                .isEqualTo(99);
    }
}
