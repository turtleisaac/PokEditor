package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link FormatModel} whose column layout is supplied by the test rather than baked in, so a
 * deliberately awkward arrangement of cell types (a custom column in the <em>middle</em>, combo
 * boxes on both sides of it, checkboxes and integers interleaved) can be built on demand.
 * <p>
 * Every read is a pure function of {row, column} and the entry payload, so any difference in the
 * model between two points in time is necessarily a write performed by the code under test.
 * <p>
 * Column names are drawn from real keys in the sheet-strings bundle because {@code FormatModel}'s
 * constructor resolves every column name eagerly; the choice of key carries no meaning here.
 */
public class E_LayoutModel extends FormatModel<E_Entry, E_LayoutModel.Column>
{
    /**
     * {@code FormatModel}'s constructor calls {@link #getColumnCount()} and
     * {@link #getNumFrozenColumns()} before this subclass's fields exist, so the shape has to be
     * parked somewhere the superclass constructor can already see it.
     */
    private static final ThreadLocal<CellTypes[]> PENDING_LAYOUT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PENDING_FROZEN = new ThreadLocal<>();

    static final String[] FROZEN_KEYS = {"id", "name"};
    static final String[] EDIT_KEYS = {"hp", "atk", "def", "speed", "spAtk", "spDef", "type", "catchRate", "expDrop", "move", "level", "hpEvYield"};

    /** how many distinct values a combo column's value may take; keeps indices inside the name lists */
    public static final int VALUE_MODULUS = 3;

    private CellTypes[] layout;
    private Integer frozen;

    private E_LayoutModel(List<E_Entry> data)
    {
        super(data, Collections.emptyList());
        this.layout = PENDING_LAYOUT.get();
        this.frozen = PENDING_FROZEN.get();
    }

    public static E_LayoutModel create(CellTypes[] layout, int frozenColumnCount, int rowCount)
    {
        PENDING_LAYOUT.set(layout);
        PENDING_FROZEN.set(frozenColumnCount);
        try
        {
            List<E_Entry> entries = new ArrayList<>();
            for (int row = 0; row < rowCount; row++)
            {
                E_Entry entry = new E_Entry(layout.length);
                for (int col = 0; col < layout.length; col++)
                    entry.set(col, (row + col) % VALUE_MODULUS);
                entries.add(entry);
            }
            return new E_LayoutModel(entries);
        }
        finally
        {
            PENDING_LAYOUT.remove();
            PENDING_FROZEN.remove();
        }
    }

    private CellTypes[] layout()
    {
        return layout != null ? layout : PENDING_LAYOUT.get();
    }

    private int frozen()
    {
        return frozen != null ? frozen : PENDING_FROZEN.get();
    }

    public CellTypes[] layoutArray()
    {
        return layout();
    }

    @Override
    public int getNumFrozenColumns()
    {
        return frozen();
    }

    @Override
    public int getColumnCount()
    {
        return layout().length;
    }

    @Override
    public String getColumnNameKey(int columnIndex)
    {
        if (columnIndex < 0)
            return FROZEN_KEYS[Math.floorMod(frozen() + columnIndex, FROZEN_KEYS.length)];
        return EDIT_KEYS[columnIndex % EDIT_KEYS.length];
    }

    @Override
    public CellTypes getCellType(int columnIndex)
    {
        return layout()[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        CellTypes type = layout()[columnIndex];
        if (type == CellTypes.CHECKBOX)
            return ((rowIndex + columnIndex) % 2) == 0;
        if (type == CellTypes.STRING)
            return "s" + rowIndex + ":" + columnIndex;
        return getData().get(rowIndex).get(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        CellTypes type = layout()[columnIndex];
        if (type == CellTypes.STRING)
            return;
        if (type == CellTypes.CHECKBOX)
        {
            boolean flag = aValue instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(aValue));
            getData().get(rowIndex).set(columnIndex, flag ? 1 : 0);
            return;
        }
        getData().get(rowIndex).set(columnIndex, Integer.parseInt(String.valueOf(aValue).trim()));
    }

    @Override
    public Object getValueFor(int entryIdx, Column property)
    {
        throw new UnsupportedOperationException("this double addresses cells by index, not by property");
    }

    @Override
    public void setValueFor(Object aValue, int entryIdx, Column property)
    {
        throw new UnsupportedOperationException("this double addresses cells by index, not by property");
    }

    @Override
    public FormatModel<E_Entry, Column> getFrozenColumnModel()
    {
        return frozen() == 0 ? null : E_FrozenModel.create(frozen(), getData());
    }

    public enum Column
    {
        CELL
    }

    /**
     * The parallel model behind the frozen ID/Name columns. It has the shape the real ones have -
     * as many columns as the parent declares frozen - and returns values which name their own
     * coordinates, so an export can be checked for having actually visited them.
     */
    public static class E_FrozenModel extends FormatModel<E_Entry, Column>
    {
        private static final ThreadLocal<Integer> PENDING_WIDTH = new ThreadLocal<>();

        private Integer width;

        private E_FrozenModel(List<E_Entry> data)
        {
            super(data, Collections.emptyList());
            this.width = PENDING_WIDTH.get();
        }

        static E_FrozenModel create(int width, List<E_Entry> data)
        {
            PENDING_WIDTH.set(width);
            try
            {
                return new E_FrozenModel(data);
            }
            finally
            {
                PENDING_WIDTH.remove();
            }
        }

        private int width()
        {
            return width != null ? width : PENDING_WIDTH.get();
        }

        /** the value frozen cell (row, column) is defined to hold; computed by the test too */
        public static String frozenValue(int row, int column)
        {
            return "frz[" + row + "," + column + "]";
        }

        /** the human-readable name frozen column {@code column} is defined to carry */
        public static String frozenName(int column)
        {
            return "FROZEN#" + column;
        }

        @Override
        public int getNumFrozenColumns()
        {
            return width();
        }

        @Override
        public int getColumnCount()
        {
            return width();
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            if (columnIndex < 0)
                return FROZEN_KEYS[Math.floorMod(width() + columnIndex, FROZEN_KEYS.length)];
            return EDIT_KEYS[columnIndex % EDIT_KEYS.length];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return frozenValue(rowIndex, columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
        }

        @Override
        public Object getValueFor(int entryIdx, Column property)
        {
            throw new UnsupportedOperationException("this double addresses cells by index, not by property");
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, Column property)
        {
            throw new UnsupportedOperationException("this double addresses cells by index, not by property");
        }

        @Override
        public FormatModel<E_Entry, Column> getFrozenColumnModel()
        {
            return null;
        }
    }

    /** unused by this double, but referenced so the import is meaningful to a reader */
    static List<TextBankData> noTextBanks()
    {
        return Collections.emptyList();
    }
}
