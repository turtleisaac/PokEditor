package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.TableCellComponents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * What an editor hands back has to be something the write path will accept.
 * <p>
 * Both halves of this were already covered and both were green while the sheet was broken. The
 * editors were tested on their own - does a cleared selection give back the original value - and
 * {@link FormatModel#prepareObjectForWriting} was tested on its own, that a column refuses what
 * it cannot store. What nobody tested was the join, and the join was the bug: type-to-search
 * leaves a combo box with nothing selected whenever the typed text matches no entry exactly, the
 * editor reported that as -1, and -1 is outside every column's range, so the range check
 * rejected it. The user typed a move name and got an error dialog.
 * <p>
 * So these tests run the editor the sheet actually installs for a column - through
 * {@link TableCellComponents#forType}, so the wiring is covered too, not a hand-picked editor -
 * and push whatever it produces into the write path for that same column. The assertion is the
 * symptom the user reported: an edit that changed nothing must not raise anything, and must
 * leave the cell as it was.
 */
class EditorToWritePathTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static final FormatModel<?, ?> MODEL = new WriteOnlyModel();

    private static final String[][] MOVES = {{"Pound", "Karate Chop", "Double Slap", "Thunderbolt"}};

    /** move IDs are 9 bits, so a sheet column offering move names declares this range */
    private static final int[] MOVE_RANGE = {0, 511};

    private static TableCellEditor editorFor(CellTypes type, int[] range)
    {
        TableCellComponents.Pair pair = TableCellComponents.forType(type, MOVES, range, null);
        assertThat(pair.editor()).as("the sheet installs an editor for a %s column", type).isNotNull();
        return pair.editor();
    }

    /**
     * The reported bug, end to end, for every column type whose editor is a combo box. Each is
     * opened on a value the column legally holds, then put into the state type-to-search leaves
     * behind when the typed text matches nothing - and what comes out has to survive the write.
     */
    @Test
    @DisplayName("typing a name that matches nothing leaves the cell alone instead of erroring")
    void aSearchThatMatchedNothingDoesNotReachTheRangeCheck()
    {
        for (CellTypes type : new CellTypes[] {
                CellTypes.COMBO_BOX, CellTypes.COLORED_COMBO_BOX, CellTypes.BITFIELD_COMBO_BOX })
        {
            // BITFIELD_COMBO_BOX stores a single set bit, so 4 is entry 3 there and entry 4 in
            // the others; either way it is a value the column can hold and the cell already has
            Object stored = 4;
            TableCellEditor editor = editorFor(type, MOVE_RANGE);

            JComboBox<?> box = (JComboBox<?>) editor.getTableCellEditorComponent(
                    new JTable(), stored, false, 0, 0);
            box.setSelectedIndex(-1);

            Object committed = editor.getCellEditorValue();

            assertThatCode(() -> MODEL.prepareObjectForWriting(committed, type, MOVE_RANGE))
                    .as("a %s edit that selected nothing must not be rejected by the write path - "
                            + "it produced %s", type, committed)
                    .doesNotThrowAnyException();

            assertThat(MODEL.prepareObjectForWriting(committed, type, MOVE_RANGE))
                    .as("and the cell must still hold what it held before the edit, for %s", type)
                    .isEqualTo(stored);
        }
    }

    /**
     * The other end of the same join. A blank Learnsets cell is null, and the numeric editor used
     * to hand back {@code String.valueOf(null)} - the four letters "null" - which the write path
     * then refused as not a number. It now declines to commit at all, so nothing reaches the
     * write path; this pins that, because "commits nothing" and "commits something harmless" are
     * not the same and only the first avoids padding the row.
     */
    @Test
    @DisplayName("a blank numeric cell left alone never reaches the write path")
    void aBlankNumericCellCommitsNothing()
    {
        TableCellEditor editor = editorFor(CellTypes.INTEGER, new int[] {0, 100});
        editor.getTableCellEditorComponent(new JTable(), null, false, 0, 0);

        assertThatCode(editor::stopCellEditing)
                .as("no dialog for an edit that changed nothing")
                .doesNotThrowAnyException();
        assertThat(editor.stopCellEditing())
                .as("a blank cell left blank must not be committed - the Learnsets write path pads "
                        + "every entry up to the one being set, so a write here invents moves")
                .isFalse();
    }

    /** A genuine selection still has to survive the same trip, or the fix swallowed real edits. */
    @Test
    @DisplayName("a real selection still reaches the write path unchanged")
    void aRealSelectionStillCommits()
    {
        TableCellEditor editor = editorFor(CellTypes.COMBO_BOX, MOVE_RANGE);

        JComboBox<?> box = (JComboBox<?>) editor.getTableCellEditorComponent(
                new JTable(), 0, false, 0, 0);
        box.setSelectedIndex(3);

        assertThat(MODEL.prepareObjectForWriting(editor.getCellEditorValue(), CellTypes.COMBO_BOX, MOVE_RANGE))
                .isEqualTo(3);
    }

    /** the smallest thing that can answer prepareObjectForWriting; no Core types involved */
    private static class WriteOnlyModel extends FormatModel<GenericFileData, CellTypes>
    {
        WriteOnlyModel() { super(java.util.List.of(), java.util.List.of()); }

        @Override public String getColumnNameKey(int columnIndex) { return null; }
        @Override public int getColumnCount() { return 0; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) { return null; }
        @Override public FormatModel<GenericFileData, CellTypes> getFrozenColumnModel() { return null; }
        @Override public Object getValueFor(int rowIdx, CellTypes property) { return null; }
        @Override public void setValueFor(Object aValue, int rowIdx, CellTypes property) { }
    }
}
