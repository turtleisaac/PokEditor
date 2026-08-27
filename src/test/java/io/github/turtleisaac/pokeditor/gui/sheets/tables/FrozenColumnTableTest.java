package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code FrozenColumnTable} is the left-hand, non-scrolling half of a sheet: the ID and Name
 * columns. Three separate contracts meet in it.
 *
 * <ul>
 *   <li><b>The corner header names the frozen columns.</b> The corner sits above the frozen block,
 *       so cell {@code c} of its single row is the header of frozen column {@code c}. Whatever
 *       index arithmetic it uses internally, the index it finally hands to
 *       {@code TableModel.getColumnName} must be a legal column index - {@code [0, columnCount)} -
 *       because that is the entire domain on which {@code getColumnName} is defined.</li>
 *   <li><b>A write is dirtying, and only a real write is.</b> "Unsaved changes" is a claim about
 *       whether the in-memory data differs from what is on disk. A write which succeeded makes it
 *       true; a write which was rejected leaves it exactly as it was.</li>
 *   <li><b>A write is local.</b> Writing cell (r, c) is an update of one coordinate. Every other
 *       coordinate is unchanged - the same injectivity property every cell write in this editor
 *       has to satisfy.</li>
 * </ul>
 */
public class FrozenColumnTableTest
{
    private static final int ROWS = 4;
    private static final int COLUMNS = 2;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void startClean()
    {
        DataManager.markClean(TextBankData.class);
    }

    /**
     * Whether {@code DataManager} currently considers the given data class dirty. {@code DataManager}
     * exposes only a process-wide {@code hasUnsavedChanges()}, which any other test in the same JVM
     * could have set, so the per-class set is read directly to keep this test independent of the
     * rest of the suite.
     */
    private static boolean dirty(Class<? extends GenericFileData> dataClass)
    {
        try
        {
            Field field = DataManager.class.getDeclaredField("dirtyClasses");
            field.setAccessible(true);
            return ((Set<?>) field.get(null)).contains(dataClass);
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not observe DataManager's dirty set", e);
        }
    }

    // ---------------------------------------------------------------- corner header

    @Test
    @DisplayName("the corner header has one cell per frozen column")
    void cornerHeaderIsAsWideAsTheFrozenBlock()
    {
        E_GridModel model = new E_GridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);
        JTable corner = table.getCornerTableHeader();

        // PROPERTY: the corner is the header of the frozen block, so it is a 1 x frozenColumnCount
        // strip. Any other width would leave a frozen column unlabelled or label a column that is
        // not there.
        assertThat(corner.getModel().getColumnCount())
                .as("the corner must have exactly one cell per frozen column")
                .isEqualTo(table.getColumnModel().getColumnCount());
        assertThat(corner.getModel().getRowCount())
                .as("the corner is a single header row")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("the corner header only ever asks the model for legal column indices")
    void cornerHeaderAsksForLegalColumnIndicesOnly()
    {
        E_GridModel model = new E_GridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);
        JTable corner = table.getCornerTableHeader();

        model.columnNameQueries().clear();
        for (int column = 0; column < corner.getModel().getColumnCount(); column++)
            corner.getModel().getValueAt(0, column);

        // PROPERTY (domain of getColumnName): TableModel.getColumnName is defined on [0, columnCount)
        // and on nothing else. The corner is meant to name frozen column c, so the index it passes
        // must BE c - it is asking "what is column c called?". The production expression is
        //     getModel().getColumnName(column - getColumnModel().getColumnCount())
        // which for column in [0, n) yields indices in [-n, -1]: every single one outside the domain.
        // A model which answers out-of-domain indices at all is doing so by accident, so a header
        // built this way is correct only by coincidence.
        assertThat(model.columnNameQueries())
                .as("every index handed to getColumnName must be a legal column index")
                .allSatisfy(index -> assertThat(index).isBetween(0, model.getColumnCount() - 1));
    }

    @Test
    @DisplayName("the corner header cell for frozen column c is the name of frozen column c")
    void cornerHeaderNamesTheFrozenColumns()
    {
        E_GridModel model = new E_GridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);
        JTable corner = table.getCornerTableHeader();

        // PROPERTY: the corner labels the frozen columns, left to right, in the same order they are
        // displayed. This is the user-visible consequence of the index arithmetic above.
        for (int column = 0; column < COLUMNS; column++)
        {
            assertThat(String.valueOf(corner.getModel().getValueAt(0, column)))
                    .as("corner cell %d must name frozen column %d", column, column)
                    .isEqualTo(model.getColumnName(column));
        }
    }

    @Test
    @DisplayName("with a FormatModel-shaped frozen model the corner names ID and Name")
    void cornerHeaderNamesTheFrozenColumnsForAFormatModel()
    {
        // The shape production actually uses: the frozen model reports the SAME number for its
        // column count and for its frozen-column count (see DefaultSheetPanel, which passes
        // getFormatModel().getFrozenColumnModel() straight into FrozenColumnTable).
        E_LayoutModel.E_FrozenModel model = E_LayoutModel.E_FrozenModel.create(COLUMNS, new ArrayList<>());
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);
        JTable corner = table.getCornerTableHeader();

        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);

        // PROPERTY (unchanged): frozen column 0 is the ID column and frozen column 1 is the Name
        // column, so the corner must read "ID" then "Name" - the strings the sheet bundle defines
        // for those two keys, computed here from the bundle rather than from the code under test.
        //
        // NOTE FOR THE READER: this passes only through a double negation. FormatModel.getColumnName
        // adds getNumFrozenColumns() back on, and for a frozen model that number equals its column
        // count, so subtracting the column count in the corner and adding it again in the model
        // cancels out. The cancellation is what makes the previous test's out-of-domain indices
        // harmless in production - and what makes them a trap for any model whose getColumnName
        // does not happen to carry the same offset.
        assertThat(String.valueOf(corner.getModel().getValueAt(0, 0))).isEqualTo(bundle.getString("id"));
        assertThat(String.valueOf(corner.getModel().getValueAt(0, 1))).isEqualTo(bundle.getString("name"));
    }

    // ---------------------------------------------------------------- writes

    @Test
    @DisplayName("a successful write stores the value and marks the text banks dirty")
    void successfulWriteStoresAndDirties()
    {
        E_GridModel model = new E_GridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);

        assertThat(dirty(TextBankData.class)).as("precondition: nothing dirty yet").isFalse();

        table.setValueAt("Bulbasaur", 2, 1);

        // PROPERTY: the frozen Name column is a view onto the parallel name bank, so editing it
        // edits TextBankData. Two things follow, and both must hold: the value has to actually be
        // stored (a write that dirties without storing loses the edit), and the data has to be
        // recorded as differing from disk (a store that does not dirty loses the edit at exit,
        // silently, because the tool never offers to save it).
        assertThat(model.get(2, 1)).as("the written value must be stored").isEqualTo("Bulbasaur");
        assertThat(dirty(TextBankData.class))
                .as("a write to the frozen name column must mark TextBankData dirty")
                .isTrue();
    }

    @Test
    @DisplayName("a write to (r, c) leaves every other cell untouched")
    void writeIsLocal()
    {
        E_GridModel model = new E_GridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);
        List<String> before = model.snapshot();

        table.setValueAt("Chikorita", 1, 1);

        // PROPERTY (locality / injectivity of the write): setValueAt(v, r, c) is an update of the
        // single coordinate (r, c). Every other coordinate must be a fixed point of it. A write that
        // spills sideways is how a name bank ends up labelling the wrong entries.
        List<String> after = model.snapshot();
        for (int row = 0; row < ROWS; row++)
        {
            for (int column = 0; column < COLUMNS; column++)
            {
                int index = row * COLUMNS + column;
                if (row == 1 && column == 1)
                    continue;
                assertThat(after.get(index))
                        .as("cell (%d, %d) must be unchanged by a write to (1, 1)", row, column)
                        .isEqualTo(before.get(index));
            }
        }
        assertThat(model.get(1, 1)).isEqualTo("Chikorita");
    }

    @Test
    @DisplayName("a rejected write does not leave the text banks falsely marked dirty")
    void rejectedWriteDoesNotDirty()
    {
        E_ThrowingGridModel model = new E_ThrowingGridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);

        assertThat(dirty(TextBankData.class)).as("precondition: nothing dirty yet").isFalse();

        try
        {
            table.setValueAt("rejected", 0, 1);
        }
        catch (Throwable expectedInHeadless)
        {
            // The production error path hard-codes JOptionPane.showMessageDialog, which cannot run
            // headlessly; see the disabled test below. What is being asserted here is the state
            // BEFORE that dialog is reached, which the throw does not disturb.
        }

        // PROPERTY: "dirty" means the in-memory data differs from what was loaded. A write the
        // underlying data rejected changed nothing, so it must not make that claim - otherwise the
        // user is prompted to save a file that has no edits in it, and (worse) learns to dismiss the
        // prompt. DefaultTable/FrozenColumnTable order this correctly: markDirty runs only after
        // super.setValueAt returns normally.
        assertThat(dirty(TextBankData.class))
                .as("a rejected write must not mark the text banks dirty")
                .isFalse();
        assertThat(model.get(0, 1)).as("a rejected write must not have stored anything").isEqualTo("cell[0,1]");
    }

    @Test
    @Disabled("""
            TESTABILITY DEFECT, not a skipped assertion.
            FrozenColumnTable.setValueAt catches the RuntimeException a rejecting model throws and
            reports it with a hard-coded JOptionPane.showMessageDialog(this, ...). In a headless JVM
            that call itself throws HeadlessException from INSIDE the catch block, so the very
            exception the catch exists to contain is replaced by a different one and still escapes.
            The property below - that a rejected write is contained and never reaches the caller -
            is therefore unassertable as the code stands, in headless CI and equally in any
            environment where the dialog cannot be shown.
            The fix is not a test-side workaround: the error path needs to delegate to an injectable
            error reporter (a field defaulting to the JOptionPane call) so a test can substitute a
            recording one. Until then only the pre-dialog state is observable, which is what
            rejectedWriteDoesNotDirty() checks.""")
    @DisplayName("a rejected write is contained and never reaches the caller")
    void rejectedWriteIsContained()
    {
        E_ThrowingGridModel model = new E_ThrowingGridModel(ROWS, COLUMNS);
        FrozenColumnTable<E_Entry> table = new FrozenColumnTable<>(model);

        // PROPERTY: setValueAt is called from the EDT during cell editing. An exception which
        // escapes it there is swallowed by the EDT's default handler and the user sees nothing at
        // all, which is exactly what the catch block exists to prevent. So no throwable of any kind
        // may escape this call.
        table.setValueAt("rejected", 0, 1);
    }

    // ---------------------------------------------------------------- doubles

    /**
     * A plain, honest {@link TableModel}: a rectangle of strings which name their own coordinates,
     * with names for its columns and no offset arithmetic of any kind. Every index it is asked for
     * is recorded, so the domain property above can be checked directly.
     */
    static class E_GridModel implements TableModel
    {
        private final int rows;
        private final int columns;
        private final String[][] cells;
        private final List<Integer> columnNameQueries = new ArrayList<>();

        E_GridModel(int rows, int columns)
        {
            this.rows = rows;
            this.columns = columns;
            this.cells = new String[rows][columns];
            for (int row = 0; row < rows; row++)
                for (int column = 0; column < columns; column++)
                    cells[row][column] = "cell[" + row + "," + column + "]";
        }

        String get(int row, int column)
        {
            return cells[row][column];
        }

        List<String> snapshot()
        {
            List<String> flat = new ArrayList<>();
            for (int row = 0; row < rows; row++)
                for (int column = 0; column < columns; column++)
                    flat.add(cells[row][column]);
            return flat;
        }

        /** every index this model has been asked to name, in call order */
        List<Integer> columnNameQueries()
        {
            return columnNameQueries;
        }

        @Override
        public int getRowCount()
        {
            return rows;
        }

        @Override
        public int getColumnCount()
        {
            return columns;
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            columnNameQueries.add(columnIndex);
            if (columnIndex < 0 || columnIndex >= columns)
                return "OUT_OF_DOMAIN(" + columnIndex + ")";
            return "FROZEN#" + columnIndex;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return cells[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            cells[rowIndex][columnIndex] = String.valueOf(aValue);
        }

        @Override
        public void addTableModelListener(TableModelListener l)
        {
        }

        @Override
        public void removeTableModelListener(TableModelListener l)
        {
        }
    }

    /** the same grid, but every write is rejected the way a real format rejects invalid data */
    static class E_ThrowingGridModel extends E_GridModel
    {
        E_ThrowingGridModel(int rows, int columns)
        {
            super(rows, columns);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            throw new IllegalArgumentException("the value \"" + aValue + "\" is not a legal name");
        }
    }
}
