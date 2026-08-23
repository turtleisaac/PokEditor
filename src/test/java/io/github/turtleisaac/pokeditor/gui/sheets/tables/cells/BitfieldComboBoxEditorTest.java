package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.BitfieldComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.BitfieldStringCellRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class BitfieldComboBoxEditorTest
{
    /** entry 0 is "no bit set", entry n is bit n-1: 17 entries is the full 16 bit domain. */
    private static final String[] ITEMS = names(17);
    private static final int LAST_INDEX = ITEMS.length - 1;

    private final JTable table = table();
    private final BitfieldComboBoxEditor editor = new BitfieldComboBoxEditor(ITEMS);

    /** The encoding itself, written out from its definition rather than taken from the code. */
    private static int bitValueOf(int index)
    {
        return index == 0 ? 0 : 1 << (index - 1);
    }

    private JComboBox<?> comboOpenedOn(Object value)
    {
        return (JComboBox<?>) editor.getTableCellEditorComponent(table, value, false, 1, 0);
    }

    @Test
    @DisplayName("picking entry n stores the single bit value 1 << (n-1), and entry 0 stores 0")
    void indexMapsToItsOwnBit()
    {
        // Expectation derived from the definition of the encoding, not from running the editor.
        // An off-by-one here sets the neighbouring flag on every move in the sheet.
        JComboBox<?> combo = comboOpenedOn(0);

        for (int index = 0; index <= LAST_INDEX; index++)
        {
            combo.setSelectedIndex(index);
            assertThat(editor.getCellEditorValue()).as("value stored by entry %d", index).isEqualTo(bitValueOf(index));
        }
    }

    @Test
    @DisplayName("distinct entries store distinct values")
    void indexToBitIsInjective()
    {
        // Two entries collapsing onto one stored value means one of the flags can never be set,
        // and the sheet shows the wrong one for it.
        JComboBox<?> combo = comboOpenedOn(0);
        List<Object> stored = new ArrayList<>();

        for (int index = 0; index <= LAST_INDEX; index++)
        {
            combo.setSelectedIndex(index);
            stored.add(editor.getCellEditorValue());
        }

        assertThat(stored).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("opening a cell on 1 << (n-1) preselects entry n, over the whole 16 bit domain")
    void bitMapsBackToItsOwnIndex()
    {
        // The reverse direction of the same encoding. It is computed with a logarithm, so the
        // boundaries (entry 0, and the top bit) are where it is most likely to slip by one.
        for (int index = 0; index <= LAST_INDEX; index++)
        {
            JComboBox<?> combo = comboOpenedOn(bitValueOf(index));
            assertThat(combo.getSelectedIndex()).as("entry preselected for stored value %d", bitValueOf(index)).isEqualTo(index);
        }
    }

    @Test
    @DisplayName("index -> value -> index is the identity for every entry")
    void indexRoundTripIsIdentity()
    {
        // Opening a cell, touching nothing and closing it must not change what is stored.
        JComboBox<?> combo = comboOpenedOn(0);

        for (int index = 0; index <= LAST_INDEX; index++)
        {
            combo.setSelectedIndex(index);
            Object stored = editor.getCellEditorValue();
            JComboBox<?> reopened = comboOpenedOn(stored);
            assertThat(reopened.getSelectedIndex()).as("entry reached again from entry %d via stored value %s", index, stored).isEqualTo(index);
        }
    }

    @Test
    @DisplayName("value -> index -> value is the identity for every single-bit value")
    void valueRoundTripIsIdentity()
    {
        // The other direction: a stored bitfield the user never edits must come back out unchanged.
        for (int index = 0; index <= LAST_INDEX; index++)
        {
            int value = bitValueOf(index);
            comboOpenedOn(value);
            assertThat(editor.getCellEditorValue()).as("value produced by opening on %d and touching nothing", value).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("what the bitfield renderer displays for a value is the entry the editor stores that value for")
    void rendererAndEditorAgreeOnEveryEntry()
    {
        // The user picks a flag by its name and the sheet must paint that same name back. If the
        // two halves round differently, the cell reads as a flag the user never chose.
        BitfieldStringCellRenderer renderer = new BitfieldStringCellRenderer(ITEMS);
        JComboBox<?> combo = comboOpenedOn(0);

        for (int index = 0; index <= LAST_INDEX; index++)
        {
            String shownInDropdown = displayedTextAt(combo, index);

            selectByDisplayedText(combo, shownInDropdown);
            Object stored = editor.getCellEditorValue();

            JLabel painted = (JLabel) renderer.getTableCellRendererComponent(table, stored, false, false, 1, 0);
            assertThat(painted.getText())
                    .as("cell text after picking entry %d ('%s'), which stored %s", index, shownInDropdown, stored)
                    .isEqualTo(shownInDropdown);
        }
    }

    @Test
    @DisplayName("opening a cell on a bitfield outside the declared entries does not throw")
    void openingOnAnUndeclaredBitfieldDoesNotThrow()
    {
        // A bitfield with several bits set, an undeclared high bit, or a null is reachable from a
        // hacked ROM or a paste. Throwing escapes onto the EDT on a double click and leaves a cell
        // the user can never repair.
        List<Object> values = Arrays.asList(null, -1, 0b101, 1 << 20, Integer.MIN_VALUE, Integer.MAX_VALUE, "1", 3.5d);

        List<String> failures = failuresOver(values,
                value -> editor.getTableCellEditorComponent(table, value, false, 1, 0));

        assertThat(failures).as("stored values this editor cannot open a cell on").isEmpty();
    }
}
