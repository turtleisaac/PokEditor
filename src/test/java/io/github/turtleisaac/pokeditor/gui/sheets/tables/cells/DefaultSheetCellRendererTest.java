package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.DefaultSheetCellRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Color;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultSheetCellRendererTest
{
    private final JTable table = table();
    private final DefaultSheetCellRenderer renderer = new DefaultSheetCellRenderer();

    @Test
    @DisplayName("renders a component for every value, in every selection state, without throwing")
    void totalOverEveryValueAndState()
    {
        // This is the base class every other sheet renderer inherits from - if it can be made to
        // throw, so can all of them, and the sheet stops painting.
        List<String> failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));
        assertThat(failures).as("values unrenderable in the unselected state").isEmpty();

        failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, true, true, 0, 0));
        assertThat(failures).as("values unrenderable in the selected state").isEmpty();

        table.setRowSelectionInterval(2, 2);
        failures = failuresOver(hostileValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 2, 0));
        assertThat(failures).as("values unrenderable on the table's selected row").isEmpty();
    }

    @Test
    @DisplayName("displays the value it was given, and blanks the cell for null")
    void displaysTheValueItWasGiven()
    {
        // The striping/selection logic must not cost the cell its content: a renderer that paints
        // the right colour and the wrong text is still a sheet that lies.
        for (Object value : List.of(42, "text", 3.5d, Boolean.TRUE))
        {
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
            assertThat(label.getText()).as("text shown for %s", describe(value)).isEqualTo(String.valueOf(value));
        }

        JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, null, false, false, 1, 0);
        assertThat(label.getText()).as("text shown for null").isEmpty();
    }

    @Test
    @DisplayName("gives adjacent unselected rows different backgrounds, so the striping is visible")
    void alternatingRowsAreDistinguishable()
    {
        // The point of overriding the default renderer at all. Sentinel colours are installed for
        // the theme keys the sheet stripes with, so the property under test is "adjacent rows are
        // painted differently" rather than "the current look and feel happens to define them".
        Color plain = new Color(1, 2, 3);
        Color stripe = new Color(4, 5, 6);
        table.setBackground(plain);
        UIManager.put("TableHeader.pressedBackground", stripe);
        try
        {
            table.clearSelection();
            Color even = renderer.getTableCellRendererComponent(table, "x", false, false, 0, 0).getBackground();
            Color odd = renderer.getTableCellRendererComponent(table, "x", false, false, 1, 0).getBackground();

            assertThat(even).as("background of an even row").isNotNull();
            assertThat(odd).as("background of an odd row").isNotNull();
            assertThat(even).as("even/odd row backgrounds").isNotEqualTo(odd);
        }
        finally
        {
            UIManager.put("TableHeader.pressedBackground", null);
        }
    }

    @Test
    @DisplayName("a cell rendered as unselected after a selected one does not keep the selected styling")
    void stylingDoesNotLeakBetweenCells()
    {
        // One renderer instance paints every cell in the column in turn. State set for a selected
        // cell that is not reset makes whole swathes of the sheet look selected.
        table.clearSelection();
        JComponent first = (JComponent) renderer.getTableCellRendererComponent(table, "x", false, false, 1, 0);
        Color backgroundFirst = first.getBackground();
        javax.swing.border.Border borderFirst = first.getBorder();

        renderer.getTableCellRendererComponent(table, "x", true, true, 1, 0);

        JComponent again = (JComponent) renderer.getTableCellRendererComponent(table, "x", false, false, 1, 0);
        assertThat(again.getBackground()).as("background of an unselected cell painted after a selected one").isEqualTo(backgroundFirst);
        assertThat(again.getBorder()).as("border of an unselected cell painted after a selected one").isEqualTo(borderFirst);
    }
}
