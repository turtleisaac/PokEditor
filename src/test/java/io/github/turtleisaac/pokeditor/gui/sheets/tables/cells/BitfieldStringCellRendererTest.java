package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.BitfieldStringCellRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class BitfieldStringCellRendererTest
{
    /** index 0 means "no bit set"; index n means bit n-1, so 17 entries covers a full 16 bit field. */
    private static final String[] ITEMS = names(17);

    private final JTable table = table();
    private final BitfieldStringCellRenderer renderer = new BitfieldStringCellRenderer(ITEMS);

    @Test
    @DisplayName("renders a component for every value a painting table could hand it, without throwing")
    void totalOverEveryValue()
    {
        // Runs inside paint(): one value it cannot survive makes the whole sheet unpaintable.
        // The hostile list includes values with several bits set and values with none of the
        // declared bits - both reachable from a hacked ROM or a paste.
        List<Object> values = new ArrayList<>(hostileValues());
        values.add(0b101);
        values.add(0xFFFF);
        values.add(1 << 20);

        List<String> failures = failuresOver(values,
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));

        assertThat(failures).as("values this bitfield renderer cannot render").isEmpty();
    }

    @Test
    @DisplayName("displays entry n for the single-bit value 1 << (n-1), across the whole 16 bit field")
    void singleBitValueDisplaysItsOwnEntry()
    {
        // Expectation comes from the definition of the encoding (value == 1 << (index - 1)),
        // not from running the renderer. If the shift or the off-by-one drifts, every bitfield
        // column in the sheet names the wrong flag.
        for (int index = 1; index <= 16; index++)
        {
            int value = 1 << (index - 1);
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
            assertThat(label.getText()).as("text shown for value %d (bit %d)", value, index - 1).isEqualTo(ITEMS[index]);
        }
    }

    @Test
    @DisplayName("displays the zeroth entry for an empty bitfield")
    void zeroDisplaysZerothEntry()
    {
        // Zero is the boundary of the encoding: it has no bit to take a logarithm of.
        JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, 0, false, false, 1, 0);
        assertThat(label.getText()).isEqualTo(ITEMS[0]);
    }

    @Test
    @DisplayName("distinct single-bit values never display the same entry")
    void distinctBitsDisplayDistinctEntries()
    {
        // Injectivity of the display mapping: if two different stored bitfields read as the same
        // flag name, the user cannot tell them apart and edits blind.
        List<String> shown = new ArrayList<>();
        for (int index = 0; index <= 16; index++)
        {
            int value = index == 0 ? 0 : 1 << (index - 1);
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
            shown.add(label.getText());
        }
        assertThat(shown).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("a bit with no declared name never displays some other flag's name")
    void undeclaredBitShowsNoName()
    {
        // Showing a plausible but wrong flag name is how a user comes to believe a move targets
        // something it does not.
        for (int bit = ITEMS.length - 1; bit < 31; bit++)
        {
            int value = 1 << bit;
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
            assertThat(label.getText()).as("text shown for undeclared bit %d (value %d)", bit, value).isNotIn((Object[]) ITEMS);
        }
    }
}
