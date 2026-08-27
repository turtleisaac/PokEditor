package io.github.turtleisaac.pokeditor.gui.sheets;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasting into a sheet whose columns actually convert and validate their input.
 * <p>
 * Every other paste test in this package runs against a fixture whose columns are all
 * {@code CellTypes.STRING}. That was not a deliberate simplification, it was a hole:
 * {@code prepareObjectForWriting} does nothing at all for text, so those tests exercise the
 * geometry and none of the conversion. A regression that refused every spreadsheet paste
 * outright shipped straight through them.
 * <p>
 * The properties here are about what a paste means, not about how it is implemented:
 * <ul>
 *   <li>a block copied out of a spreadsheet lands, and the trailing newline every spreadsheet
 *       appends is a terminator rather than a row of data;</li>
 *   <li>a blank cell means "nothing here" and leaves the sheet's value alone, in a numeric
 *       column where no spelling of empty is a number;</li>
 *   <li>a paste is all or nothing - if any cell is refused, no cell is written, because the
 *       write path cannot roll back and there is no undo.</li>
 * </ul>
 */
class NumericPasteTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static final int MIN = 0;
    private static final int MAX = 255;

    /** Selects the whole grid and fires the paste action against the given clipboard text. */
    private static TestSheet.Model pasteInto(TestSheet.Model model, String clipboard)
    {
        TestSheet.Table table = new TestSheet.Table(model);
        table.setRowSelectionInterval(0, table.getRowCount() - 1);
        table.setColumnSelectionInterval(0, table.getColumnCount() - 1);

        // a clipboard of our own rather than the system one, which needs a display
        Clipboard board = new Clipboard("test");
        board.setContents(new StringSelection(clipboard), null);

        DefaultTable.PasteAction<GenericFileData, TestSheet.NoProperties> action =
                new DefaultTable.PasteAction<>(table)
                {
                    @Override
                    protected Clipboard getClipboard()
                    {
                        return board;
                    }
                };
        action.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "Paste"));
        return model;
    }

    @Test
    @DisplayName("a block copied out of a spreadsheet lands, trailing newline and all")
    void spreadsheetBlockLands()
    {
        // Excel, LibreOffice and Sheets all terminate the final row with a newline. Splitting
        // with a negative limit keeps the empty field after it, so a 2x2 copy arrives as three
        // rows. Treating that as data is what refused the paste outright.
        TestSheet.Model model = pasteInto(TestSheet.numericGrid(4, MIN, MAX), "1\t2\r\n3\t4\r\n");

        assertThat(model.getValueAt(0, 0)).as("row 0 col 0").isEqualTo(1);
        assertThat(model.getValueAt(0, 1)).as("row 0 col 1").isEqualTo(2);
        assertThat(model.getValueAt(1, 0)).as("row 1 col 0").isEqualTo(3);
        assertThat(model.getValueAt(1, 1)).as("row 1 col 1").isEqualTo(4);
    }

    @Test
    @DisplayName("the trailing newline does not shift the block or repeat it")
    void trailingNewlineDoesNotChangeTheGeometry()
    {
        // the same paste with and without the terminator must produce the same grid: the
        // extra line would otherwise inflate the row count and change how many times the
        // block is tiled down the selection
        TestSheet.Model with = pasteInto(TestSheet.numericGrid(4, MIN, MAX), "1\t2\r\n3\t4\r\n");
        TestSheet.Model without = pasteInto(TestSheet.numericGrid(4, MIN, MAX), "1\t2\r\n3\t4");

        assertThat(with.snapshot()).isDeepEqualTo(without.snapshot());
    }

    @Test
    @DisplayName("a blank cell leaves the sheet's value alone rather than being parsed")
    void blankCellsAreSkipped()
    {
        // there is no number spelled "", so the only two options are to refuse the paste or to
        // read the cell as absent. absent is what a spreadsheet means by it.
        TestSheet.Model model = pasteInto(TestSheet.numericGrid(4, MIN, MAX), "1\t\r\n\t4");

        assertThat(model.getValueAt(0, 0)).isEqualTo(1);
        assertThat(model.getValueAt(1, 1)).isEqualTo(4);
        assertThat(String.valueOf(model.getValueAt(0, 1)))
                .as("a blank cell must not overwrite what was there")
                .isEqualTo(String.valueOf(TestSheet.UNTOUCHED));
        assertThat(String.valueOf(model.getValueAt(1, 0)))
                .as("a blank cell must not overwrite what was there")
                .isEqualTo(String.valueOf(TestSheet.UNTOUCHED));
    }

    @Test
    @DisplayName("one out-of-range cell refuses the whole paste, leaving every cell untouched")
    void oneBadValueWritesNothing()
    {
        // the write loop cannot roll back and there is no undo, so a paste that gave up part
        // way through would leave the sheet holding some of the block and not the rest, with
        // nothing to say which. 300 does not fit the declared range.
        TestSheet.Model model = TestSheet.numericGrid(4, MIN, MAX);
        String[][] before = model.snapshot();

        pasteInto(model, "1\t2\r\n300\t4");

        assertThat(model.snapshot())
                .as("no cell may be written when any cell is refused")
                .isDeepEqualTo(before);
    }

    @Test
    @DisplayName("a name pasted into a numeric column refuses the whole paste")
    void nonNumericTextWritesNothing()
    {
        // the sheet exports rendered text, so an exported column is full of names; pasting one
        // back must fail cleanly rather than half-applying
        TestSheet.Model model = TestSheet.numericGrid(4, MIN, MAX);
        String[][] before = model.snapshot();

        pasteInto(model, "1\t2\r\nBulbasaur\t4");

        assertThat(model.snapshot()).isDeepEqualTo(before);
    }

    @Test
    @DisplayName("the bounds of the declared range are accepted")
    void boundsAreAccepted()
    {
        // an exclusive comparison in the validator shows up here first
        TestSheet.Model model = pasteInto(TestSheet.numericGrid(4, MIN, MAX), MIN + "\t" + MAX);

        assertThat(model.getValueAt(0, 0)).isEqualTo(MIN);
        assertThat(model.getValueAt(0, 1)).isEqualTo(MAX);
    }

    @Test
    @DisplayName("a paste of only a newline changes nothing and does not throw")
    void emptyClipboardIsHarmless()
    {
        TestSheet.Model model = TestSheet.numericGrid(4, MIN, MAX);
        String[][] before = model.snapshot();

        pasteInto(model, "\r\n");

        assertThat(model.snapshot()).isDeepEqualTo(before);
    }
}
