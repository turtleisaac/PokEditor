package io.github.turtleisaac.pokeditor.gui.sheets;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * The smallest sheet the production table will accept: a fixed grid of plain strings, no frozen
 * columns, no cell editors, no text banks and no format data behind it. Everything about the
 * sheet's own semantics is deliberately trivial so that a test which fails here has failed in the
 * table, not in whatever a real sheet would have done with the value.
 */
final class TestSheet
{
    /** Fixed, because {@link FormatModel}'s constructor asks for the column count before a subclass field could hold it. */
    static final int COLUMNS = 6;

    static final String UNTOUCHED = ".";

    private TestSheet() {}

    enum NoProperties { NONE }

    static final class Model extends FormatModel<GenericFileData, NoProperties>
    {
        final Object[][] cells;

        Model(int rows)
        {
            super(Arrays.asList(new GenericFileData[rows]), Collections.emptyList());
            cells = new Object[rows][COLUMNS];
            for (Object[] row : cells)
                Arrays.fill(row, UNTOUCHED);
        }

        /** A snapshot of the grid, so an assertion cannot be fooled by later writes. */
        String[][] snapshot()
        {
            String[][] copy = new String[cells.length][COLUMNS];
            for (int row = 0; row < cells.length; row++)
                for (int col = 0; col < COLUMNS; col++)
                    copy[row][col] = String.valueOf(cells[row][col]);
            return copy;
        }

        @Override public int getColumnCount() { return COLUMNS; }
        @Override public Object getValueAt(int row, int column) { return cells[row][column]; }
        @Override public void setValueAt(Object value, int row, int column)
        {
            // mirrors the real sheets, whose setValueAt reaches setValueFor and converts there.
            // A double that stored the raw String would report a paste as landing while the
            // conversion it is supposed to be exercising never ran.
            cells[row][column] = prepareObjectForWriting(value, getCellType(column),
                    cellTypes == null ? null : getCellValueRange(column));
        }
        @Override public String getColumnNameKey(int columnIndex) { return "hp"; }
        @Override public FormatModel<GenericFileData, NoProperties> getFrozenColumnModel() { return null; }
        /**
         * The cell type each column reports. STRING by default, because most of the geometry
         * tests only care about which cell a value lands in - but a sheet made entirely of
         * STRING columns cannot exercise validation at all, since prepareObjectForWriting does
         * nothing for text. A paste regression shipped behind exactly that gap, so a fixture
         * can now declare real types.
         */
        CellTypes[] cellTypes;

        @Override public CellTypes getCellType(int columnIndex)
        {
            return cellTypes == null ? CellTypes.STRING : cellTypes[columnIndex];
        }

        @Override public int[] getCellValueRange(int columnIndex)
        {
            return valueRange == null ? super.getCellValueRange(columnIndex) : valueRange;
        }

        int[] valueRange;
        @Override public Object getValueFor(int entryIdx, NoProperties property) { return null; }
        @Override public void setValueFor(Object value, int entryIdx, NoProperties property) { }
    }

    static final class Table extends DefaultTable<GenericFileData, NoProperties>
    {
        Table(Model model)
        {
            super(model, Collections.emptyList(), new int[] {40, 40, 40, 40, 40, 40}, null);
        }

        @Override public Queue<String[]> obtainTextSources(List<TextBankData> textData) { return new LinkedList<>(); }
        @Override public Class<GenericFileData> getDataClass() { return GenericFileData.class; }
    }

    /**
     * A grid whose columns are all numeric, bounded by the given inclusive range. Use this
     * wherever a test needs the write path to actually convert and validate.
     */
    static Model numericGrid(int rows, int min, int max)
    {
        Model model = new Model(rows);
        model.cellTypes = new CellTypes[COLUMNS];
        Arrays.fill(model.cellTypes, CellTypes.INTEGER);
        model.valueRange = new int[] {min, max};
        return model;
    }

    /** A grid of the given size with every cell marked as never having been written. */
    static String[][] untouchedGrid(int rows)
    {
        String[][] grid = new String[rows][COLUMNS];
        for (String[] row : grid)
            Arrays.fill(row, UNTOUCHED);
        return grid;
    }
}
