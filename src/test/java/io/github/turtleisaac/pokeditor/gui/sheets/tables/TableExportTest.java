package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.CheckBoxRenderer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code DefaultTable.exportClean} and {@code DefaultTable.exportEditable} are the two projections
 * a user can take of a sheet. Both are pure <em>reads</em>, and both are total functions of the
 * table, which pins down four things independently of what the code happens to do today:
 *
 * <ul>
 *   <li><b>Shape.</b> The clean export is a rectangle of exactly
 *       {@code rowCount x (frozenColumnCount + columnCount)}; the editable export is
 *       {@code rowCount x columnCount}. Rectangularity is part of it - the consumers index these
 *       arrays by column, so a short row is an exception at some later, unrelated point.</li>
 *   <li><b>Totality.</b> Every cell of the clean export is filled. A null is not "no value", it is
 *       a column the walk never visited, which is precisely how the frozen ID/Name columns went
 *       missing when only {@code getColumnModel()} was iterated.</li>
 *   <li><b>Alignment.</b> The clean export contains the editable export as a sub-block starting at
 *       column {@code frozenColumnCount}. Where a column has no renderer of its own, the two must
 *       agree cell for cell under that shift, since both then read the same model value.</li>
 *   <li><b>Purity.</b> Exporting reads; it must leave the model bit-for-bit identical. An export
 *       which grows a list while walking it is the same defect class as a paint which does.</li>
 * </ul>
 */
public class TableExportTest
{
    /** the same awkward mix used for the text-source properties, so exports are exercised over
     *  rendered columns (combo, custom) and un-rendered ones (integer, string) alike */
    private static final CellTypes[] LAYOUT = {
            CellTypes.CHECKBOX,             // 0 - CheckBoxRenderer: exportClean falls back to the raw value
            CellTypes.COMBO_BOX,            // 1 - rendered
            CellTypes.INTEGER,              // 2 - no renderer: exportClean falls back to the raw value
            CellTypes.COLORED_COMBO_BOX,    // 3 - rendered
            CellTypes.CUSTOM,               // 4 - rendered
            CellTypes.STRING,               // 5 - no renderer: exportClean falls back to the raw value
            CellTypes.COMBO_BOX,            // 6 - rendered
            CellTypes.BITFIELD_COMBO_BOX,   // 7 - rendered
            CellTypes.CUSTOM,               // 8 - rendered (shares column 4's renderer)
            CellTypes.COMBO_BOX             // 9 - rendered
    };

    /** the columns for which exportClean is defined to fall back to {@code String.valueOf(getValueAt(..))}:
     *  those with no renderer at all, and those whose renderer is a CheckBoxRenderer */
    private static final int[] FALLBACK_COLUMNS = {0, 2, 5};

    private static final int FROZEN = 2;
    private static final int ROWS = 5;

    private E_CustomCellSupplier supplier;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void resetStatics()
    {
        E_ProbeTable.nullPosition = -1;
        supplier = new E_CustomCellSupplier();
    }

    private E_ProbeTable build(CellTypes[] layout, int frozen, int rows)
    {
        E_ProbeTable.sourceCount = sourceCountFor(layout);
        E_LayoutModel model = E_LayoutModel.create(layout, frozen, rows);
        int[] widths = new int[frozen + layout.length];
        Arrays.fill(widths, 50);
        return new E_ProbeTable(model, widths, supplier);
    }

    /** one element per combo-box column plus one shared triple for the custom columns */
    private static int sourceCountFor(CellTypes[] layout)
    {
        int total = 0;
        boolean customClaimed = false;
        for (CellTypes type : layout)
        {
            if (type == CellTypes.COMBO_BOX || type == CellTypes.COLORED_COMBO_BOX || type == CellTypes.BITFIELD_COMBO_BOX)
                total += 1;
            else if (type == CellTypes.CUSTOM && !customClaimed)
            {
                customClaimed = true;
                total += 3;
            }
        }
        return total;
    }

    /** every model cell, plus every entry payload, as one comparable snapshot */
    private static List<String> modelSnapshot(E_LayoutModel model)
    {
        List<String> snapshot = new ArrayList<>();
        snapshot.add("rows=" + model.getRowCount());
        snapshot.add("cols=" + model.getColumnCount());
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int column = 0; column < model.getColumnCount(); column++)
                snapshot.add(row + "," + column + "=" + model.getValueAt(row, column));
            snapshot.add("payload" + row + "=" + Arrays.toString(model.getData().get(row).snapshot()));
        }
        return snapshot;
    }

    // ---------------------------------------------------------------- shape

    @Test
    @DisplayName("exportClean is a rowCount x (frozenColumnCount + columnCount) rectangle")
    void cleanExportHasTheDeclaredShape()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        String[][] clean = table.exportClean();

        // PROPERTY: the clean export is the whole visible sheet, which is the frozen block placed
        // to the left of the editable block. Its width is therefore the sum of the two widths, and
        // its height the number of rows - both counted here from the model, never from the result.
        assertThat(clean.length).as("one row per model row").isEqualTo(ROWS);
        for (int row = 0; row < clean.length; row++)
        {
            assertThat(clean[row])
                    .as("row %d must be as wide as the frozen block plus the editable block", row)
                    .hasSize(FROZEN + LAYOUT.length);
        }
    }

    @Test
    @DisplayName("exportEditable is a rowCount x columnCount rectangle")
    void editableExportHasTheDeclaredShape()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        String[][] editable = table.exportEditable();

        // PROPERTY: the editable export is exactly the editable columns - the frozen block is not
        // part of it, so its width is the table's column count and nothing else.
        assertThat(editable.length).isEqualTo(ROWS);
        for (int row = 0; row < editable.length; row++)
        {
            assertThat(editable[row])
                    .as("row %d of the editable export must be as wide as the table", row)
                    .hasSize(LAYOUT.length);
        }
    }

    // ---------------------------------------------------------------- totality

    @Test
    @DisplayName("exportClean has no holes: every cell is filled")
    void cleanExportHasNoHoles()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        String[][] clean = table.exportClean();

        // PROPERTY (totality): the export is defined for every coordinate of the rectangle it
        // allocates. A null is not a value the sheet can contain - it is the signature of a column
        // (or a whole block) the walk failed to visit, and it becomes a "null" in the user's file
        // or an NPE in whatever consumes the export.
        for (int row = 0; row < clean.length; row++)
        {
            for (int column = 0; column < clean[row].length; column++)
            {
                assertThat(clean[row][column])
                        .as("exportClean[%d][%d] was never written", row, column)
                        .isNotNull();
            }
        }
    }

    @Test
    @DisplayName("exportClean carries the frozen model's own values in columns [0, frozenColumnCount)")
    void cleanExportContainsTheFrozenBlock()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        String[][] clean = table.exportClean();

        // PROPERTY: the frozen ID/Name columns live in a separate model which the table's own
        // column model knows nothing about. They are part of the sheet the user sees, so they are
        // part of the sheet the user exports; the only way they can appear is for the export to
        // read the frozen model directly. Their values are a known function of the coordinate here,
        // so their presence, their order and their placement are all checkable at once.
        for (int row = 0; row < ROWS; row++)
        {
            for (int column = 0; column < FROZEN; column++)
            {
                assertThat(clean[row][column])
                        .as("frozen cell (%d, %d) must appear at clean export column %d", row, column, column)
                        .isEqualTo(E_LayoutModel.E_FrozenModel.frozenValue(row, column));
            }
        }
    }

    // ---------------------------------------------------------------- alignment

    @Test
    @DisplayName("the editable block sits at offset frozenColumnCount inside exportClean")
    void editableBlockIsOffsetByTheFrozenWidth()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        String[][] clean = table.exportClean();
        String[][] editable = table.exportEditable();

        // PROPERTY (alignment): both exports read the same table, so editable column c is clean
        // column c + frozenColumnCount. For columns which have no renderer of their own (or a
        // checkbox renderer) both projections reduce to String.valueOf(getValueAt(row, c)), so on
        // those columns the two arrays must agree cell for cell under exactly that shift. Any other
        // offset would mean a user's two exports of the same sheet disagree about which column is
        // which.
        for (int column : FALLBACK_COLUMNS)
        {
            for (int row = 0; row < ROWS; row++)
            {
                assertThat(clean[row][FROZEN + column])
                        .as("clean[%d][%d] must be editable[%d][%d]", row, FROZEN + column, row, column)
                        .isEqualTo(editable[row][column]);
            }
        }

        // and the fallback really is the raw model value, so the property above is not vacuous
        assertThat(table.getColumnModel().getColumn(2).getCellRenderer())
                .as("an INTEGER column is expected to carry no renderer of its own")
                .isNull();
        assertThat(table.getColumnModel().getColumn(0).getCellRenderer())
                .as("a CHECKBOX column is expected to carry a CheckBoxRenderer")
                .isInstanceOf(CheckBoxRenderer.class);
        for (int row = 0; row < ROWS; row++)
        {
            assertThat(editable[row][2]).isEqualTo(String.valueOf(table.getModel().getValueAt(row, 2)));
        }
    }

    @Test
    @DisplayName("with no frozen columns the clean export is exactly as wide as the table")
    void noFrozenColumnsMeansNoOffset()
    {
        E_ProbeTable table = build(LAYOUT, 0, ROWS);
        String[][] clean = table.exportClean();
        String[][] editable = table.exportEditable();

        // PROPERTY: frozenColumnCount = 0 is the identity case of the alignment property - the
        // offset vanishes and the clean export degenerates to the same width as the editable one.
        // A table with no frozen model must not reserve, or skip, phantom columns.
        assertThat(clean.length).isEqualTo(ROWS);
        for (String[] row : clean)
            assertThat(row).hasSize(LAYOUT.length);

        for (int column : FALLBACK_COLUMNS)
        {
            for (int row = 0; row < ROWS; row++)
                assertThat(clean[row][column]).isEqualTo(editable[row][column]);
        }
    }

    // ---------------------------------------------------------------- purity

    @Test
    @DisplayName("exporting does not mutate the model")
    void exportingIsPure()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);
        E_LayoutModel model = (E_LayoutModel) table.getFormatModel();

        List<String> before = modelSnapshot(model);
        table.exportClean();
        table.exportEditable();
        table.exportClean();
        List<String> after = modelSnapshot(model);

        // PROPERTY (read purity): an export is an observation. Observations compose - taking two of
        // them, in either order, must leave the observed object in the state it started in. This is
        // the same invariant a paint has to satisfy, and it is violated by exactly the same mistake:
        // a read path which lengthens a list to have something to return.
        assertThat(after)
                .as("the model must be bit-for-bit unchanged by exporting")
                .isEqualTo(before);
    }

    @Test
    @DisplayName("exporting twice yields equal arrays")
    void exportingIsDeterministic()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, ROWS);

        String[][] first = table.exportClean();
        String[][] second = table.exportClean();

        // PROPERTY (referential transparency): if the export is pure and the model is unchanged
        // between the two calls, the two results are equal. A difference would mean the export
        // depends on hidden state carried over from the previous call - the shared renderers, say.
        assertThat(second).isDeepEqualTo(first);
    }

    // ---------------------------------------------------------------- boundaries

    @Test
    @DisplayName("a table with no rows exports an empty clean array, not an exception")
    void zeroRowsCleanExport()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, 0);

        // PROPERTY (totality at the boundary): "no rows" is a legal sheet state, and the export of
        // an empty sheet is the empty table. A total function must be defined there too.
        String[][] clean = table.exportClean();
        assertThat(clean).as("the clean export of an empty sheet is the empty array").isEmpty();
    }

    @Test
    @DisplayName("a table with no rows exports an empty editable array, not an exception")
    void zeroRowsEditableExport()
    {
        E_ProbeTable table = build(LAYOUT, FROZEN, 0);

        // PROPERTY (totality at the boundary): identical to the clean case. Note that
        // exportEditable bounds its column loop with `output[0].length` - the width of the FIRST
        // ROW - rather than with the column count it just allocated from. When there are no rows
        // there is no first row, so the bound itself is what fails. The width of a rectangle is not
        // a property of any one of its rows; deriving it from row 0 makes a total function partial
        // at exactly the boundary where a user is most likely to meet it (a freshly emptied sheet).
        String[][] editable = table.exportEditable();
        assertThat(editable).as("the editable export of an empty sheet is the empty array").isEmpty();
    }
}
