package io.github.turtleisaac.pokeditor.gui.sheets;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Paste has a geometry, and the geometry is the contract. A clipboard block of m rows by n
 * columns is stamped into the sheet starting at the top left of the selection, repeated as many
 * whole times as fit inside the selection, and never anywhere else.
 * <p>
 * Every expected grid below is written out in full, including the cells that must stay marked
 * "{@value TestSheet#UNTOUCHED}". That is deliberate: the damage this guards against was never a
 * missing write, it was an extra one - a paste of two rows into a five row selection rounding up
 * to three copies and overwriting the row below the selection, where the user was not looking.
 */
class PasteGeometryTest
{
    private static final String U = TestSheet.UNTOUCHED;

    /**
     * Selects the given rectangle, puts {@code clipboard} on the system clipboard and fires the
     * production paste action.
     *
     * @return the whole sheet afterwards, so containment can be asserted, not just coverage
     */
    private static String[][] paste(int rows, int firstRow, int lastRow, int firstCol, int lastCol, String clipboard)
    {
        TestSheet.Model model = new TestSheet.Model(rows);
        TestSheet.Table table = new TestSheet.Table(model);
        table.setRowSelectionInterval(firstRow, lastRow);
        table.setColumnSelectionInterval(firstCol, lastCol);

        SystemClipboardStub.withClipboardContents(clipboard,
                () -> new DefaultTable.PasteAction(table).actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "paste")));

        return model.snapshot();
    }

    /**
     * The commonest spreadsheet gesture there is: click one cell, paste a block. All m x n cells
     * have to land. Computing the copy count as a rounded ratio of selection to clipboard makes
     * this one round to zero copies, so the paste did nothing at all and said nothing about it.
     */
    @Test
    @DisplayName("one selected cell and an m x n clipboard writes all m x n cells from that cell")
    void singleCellSelectionTakesTheWholeClipboard()
    {
        String[][] sheet = paste(6, 1, 1, 1, 1, "A\tB\nC\tD\nE\tF");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {U,   U,   U,   U, U, U},
                {U, "A", "B",   U, U, U},
                {U, "C", "D",   U, U, U},
                {U, "E", "F",   U, U, U},
                {U,   U,   U,   U, U, U},
                {U,   U,   U,   U, U, U},
        });
    }

    @Test
    @DisplayName("one selected cell and a single clipboard cell writes exactly that one cell")
    void singleCellPasteWritesOneCell()
    {
        String[][] sheet = paste(3, 2, 2, 3, 3, "solo");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {U, U, U,      U, U, U},
                {U, U, U,      U, U, U},
                {U, U, U, "solo", U, U},
        });
    }

    /**
     * Containment, vertically. Five selected rows hold two whole copies of a two row clipboard,
     * not two and a half. Rounding 2.5 up writes a third copy, which spills one row past the
     * bottom of the selection and silently overwrites an entry the user never selected.
     */
    @Test
    @DisplayName("a selection that is not a whole multiple of the clipboard never writes past its own last row")
    void partialVerticalCopyIsNotRoundedUp()
    {
        String[][] sheet = paste(8, 0, 4, 0, 0, "P\nQ");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {"P", U, U, U, U, U},
                {"Q", U, U, U, U, U},
                {"P", U, U, U, U, U},
                {"Q", U, U, U, U, U},
                {  U, U, U, U, U, U},   // still inside the selection, but no whole copy reaches it
                {  U, U, U, U, U, U},   // below the selection entirely - the row the old paste clobbered
                {  U, U, U, U, U, U},
                {  U, U, U, U, U, U},
        });
    }

    /** The same argument along the other axis: five selected columns hold two copies of two, not three. */
    @Test
    @DisplayName("a selection that is not a whole multiple of the clipboard never writes past its own last column")
    void partialHorizontalCopyIsNotRoundedUp()
    {
        String[][] sheet = paste(2, 0, 0, 0, 4, "X\tY");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {"X", "Y", "X", "Y", U, U},
                {  U,   U,   U,   U, U, U},
        });
    }

    /**
     * Tiling. When the selection is an exact multiple of the clipboard in both directions, the
     * clipboard repeats to fill it exactly - every selected cell written, nothing outside.
     */
    @Test
    @DisplayName("a selection that is an exact multiple of the clipboard is tiled with it exactly")
    void exactMultipleSelectionIsTiled()
    {
        String[][] sheet = paste(6, 0, 5, 0, 3, "A\tB\nC\tD");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {"A", "B", "A", "B", U, U},
                {"C", "D", "C", "D", U, U},
                {"A", "B", "A", "B", U, U},
                {"C", "D", "C", "D", U, U},
                {"A", "B", "A", "B", U, U},
                {"C", "D", "C", "D", U, U},
        });
    }

    /**
     * Clipping. A paste anchored near the bottom right corner writes the part of the block that
     * fits and drops the rest, rather than throwing an index out of bounds at the user.
     */
    @Test
    @DisplayName("a paste that runs off the bottom and right edges writes what fits and does not throw")
    void pasteIsClippedAtTheSheetEdges()
    {
        assertThatCode(() -> {
            String[][] sheet = paste(3, 2, 2, 4, 4, "1\t2\t3\n4\t5\t6\n7\t8\t9");

            assertThat(sheet).isDeepEqualTo(new String[][] {
                    {U, U, U, U,   U,   U},
                    {U, U, U, U,   U,   U},
                    {U, U, U, U, "1", "2"},
            });
        }).doesNotThrowAnyException();
    }

    /**
     * A selection smaller than the clipboard still gets one whole copy. Truncating the ratio to
     * zero is the failure this rules out: two selected rows and a three row clipboard is a
     * fraction less than one, and the user expects their three rows regardless.
     */
    @Test
    @DisplayName("a selection smaller than the clipboard still receives one whole copy of it")
    void selectionSmallerThanClipboardStillPastesOnce()
    {
        String[][] sheet = paste(6, 0, 1, 0, 0, "P\nQ\nR");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {"P", U, U, U, U, U},
                {"Q", U, U, U, U, U},
                {"R", U, U, U, U, U},
                {  U, U, U, U, U, U},
                {  U, U, U, U, U, U},
                {  U, U, U, U, U, U},
        });
    }

    /**
     * The paste is anchored at the top left cell of the selection, not at the origin of the
     * sheet, and it does not reach back above or to the left of that anchor.
     */
    @Test
    @DisplayName("the paste is anchored at the top left of the selection and never reaches above or left of it")
    void pasteIsAnchoredAtTheSelectionOrigin()
    {
        String[][] sheet = paste(5, 3, 3, 2, 2, "Z");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {U, U,   U, U, U, U},
                {U, U,   U, U, U, U},
                {U, U,   U, U, U, U},
                {U, U, "Z", U, U, U},
                {U, U,   U, U, U, U},
        });
    }

    /**
     * Empty clipboard cells are cells. A copied block whose middle column is blank must not
     * collapse, or every column to its right lands one place too far left.
     */
    @Test
    @DisplayName("blank cells inside the clipboard block keep their place in the destination")
    void blankClipboardCellsKeepTheirColumn()
    {
        String[][] sheet = paste(2, 0, 0, 0, 0, "A\t\tC");

        assertThat(sheet).isDeepEqualTo(new String[][] {
                {"A", "", "C", U, U, U},
                {  U,  U,   U, U, U, U},
        });
    }
}
