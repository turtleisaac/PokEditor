package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class IndexedStringCellRendererTest
{
    /** 21 names but only 18 colours - the exact shape that made a type value of 20 fatal. */
    private static final String[] ITEMS = names(21);
    private static final Color[] COLORS = colors(18);

    private static Color[] colors(int count)
    {
        Color[] colors = new Color[count];
        for (int i = 0; i < count; i++)
            colors[i] = new Color(i * 10, 0, 0);
        return colors;
    }

    private final JTable table = table();

    @Test
    @DisplayName("renders a component for every value a painting table could hand it, without throwing")
    void totalOverEveryValue()
    {
        // A renderer runs inside paint(), once per visible cell. A value it cannot survive does
        // not produce one bad cell - it makes the entire sheet permanently unpaintable, and the
        // resulting exception storm never reaches the user.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);

        List<String> failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));

        assertThat(failures).as("values this renderer cannot render").isEmpty();
    }

    @Test
    @DisplayName("coloured variant renders every value even when the colour list is shorter than the name list")
    void coloredVariantTotalOverEveryValue()
    {
        // The regression this guards: the colour lookup indexed colors[] while the bounds check
        // was against items[]. With 21 names and 18 colours, a type of 20 killed the sheet.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(ITEMS, COLORS);

        List<String> failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));

        assertThat(failures).as("values the coloured renderer cannot render").isEmpty();
    }

    @Test
    @DisplayName("an in-range index displays that index's name, not the raw number")
    void inRangeIndexShowsItsName()
    {
        // The whole reason this renderer exists: the model stores an index, the user must see a name.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);

        for (int i = 0; i < ITEMS.length; i++)
        {
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, i, false, false, 1, 0);
            assertThat(label.getText()).as("text shown for index %d", i).isEqualTo(ITEMS[i]);
        }
    }

    @Test
    @DisplayName("an out-of-range index never displays some other entry's name")
    void outOfRangeIndexShowsNoName()
    {
        // Silently showing the wrong name is worse than showing a number: the user believes the
        // cell holds a value it does not hold, and edits around that belief.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);

        for (Object value : List.of(ITEMS.length, ITEMS.length + 7, 9999, -1, Integer.MIN_VALUE))
        {
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
            assertThat(label.getText()).as("text shown for out-of-range value %s", value).isNotIn((Object[]) ITEMS);
        }
    }

    @Test
    @DisplayName("the coloured variant paints an in-range value with that value's own colour")
    void inRangeValueGetsItsOwnColor()
    {
        // Derived from the definition of the column: colour i belongs to index i. If the mapping
        // slips, every type in the sheet is tinted as the wrong type.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(ITEMS, COLORS);

        for (int i = 0; i < COLORS.length; i++)
        {
            Component c = renderer.getTableCellRendererComponent(table, i, false, false, 1, 0);
            assertThat(c.getBackground()).as("background for index %d", i).isEqualTo(COLORS[i]);
        }
    }

    @Test
    @DisplayName("setItems re-points the renderer at the new names")
    void setItemsChangesWhatIsDisplayed()
    {
        // The sheet swaps text banks under a live table (resetIndexedCellRendererText). A renderer
        // that kept the old list would keep showing names from the previous ROM's text bank.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);
        String[] replacement = {"zero", "one", "two"};
        renderer.setItems(replacement);

        for (int i = 0; i < replacement.length; i++)
        {
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, i, false, false, 1, 0);
            assertThat(label.getText()).isEqualTo(replacement[i]);
        }
    }

    @Test
    @DisplayName("still renders every value after the name list is swapped for a shorter one")
    void totalAfterShrinkingItems()
    {
        // Indices legal against the old list are out of range against the new one; that transition
        // is exactly when a missing bounds check bites.
        IndexedStringCellRenderer renderer = new IndexedStringCellRenderer(ITEMS);
        renderer.setItems(new String[] {"only"});

        List<String> failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));

        assertThat(failures).as("values this renderer cannot render after setItems").isEmpty();
    }
}
