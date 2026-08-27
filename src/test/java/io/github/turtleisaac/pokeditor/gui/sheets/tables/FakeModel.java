package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A model which is deliberately <em>correct</em>, so that {@link FormatModelContract} can be
 * shown to be satisfiable rather than merely unsatisfiable-in-practice. It has the same
 * repeated-column shape as the learnsets and evolutions sheets, so every property in the
 * contract has something real to bite on here:
 * <ul>
 *     <li>reads never touch the entry list's length,</li>
 *     <li>the repeated group being addressed is model state, not state shared between every
 *         instance of the column enum, so two sheets open at once cannot read each other's
 *         column index,</li>
 *     <li>a write lands in one cell and nowhere else,</li>
 *     <li>the range a column advertises is the range it actually enforces.</li>
 * </ul>
 */
public class FakeModel extends FormatModel<FakeEntry, FakeModel.FakeColumn>
{
    /** how many repeated groups the grid makes room for, whatever an entry currently holds */
    public static final int MAX_GROUPS = 4;

    /**
     * Which repeated group the column index currently being served refers to.
     * <p>
     * The real sheets keep this on the column enum, where it is shared by every model in the
     * process. Keeping it per-model is the corrected version of that: it is still written
     * before each dispatch, but one sheet's painting cannot perturb another's.
     */
    private int group;

    public FakeModel(List<FakeEntry> data, List<TextBankData> textBankData)
    {
        super(data, textBankData);
    }

    /** the repeated group the call currently in flight is addressing */
    protected int currentGroup()
    {
        return group;
    }

    @Override
    public int getNumFrozenColumns()
    {
        return 2;
    }

    @Override
    public String getColumnNameKey(int columnIndex)
    {
        return FakeColumn.getColumn(columnIndex).key;
    }

    @Override
    public int getColumnCount()
    {
        return MAX_GROUPS * FakeColumn.NUMBER_OF_COLUMNS.idx;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (columnIndex >= 0)
        {
            group = columnIndex / FakeColumn.NUMBER_OF_COLUMNS.idx;
            return getValueFor(rowIndex, FakeColumn.getColumn(columnIndex % FakeColumn.NUMBER_OF_COLUMNS.idx));
        }
        return getValueFor(rowIndex, FakeColumn.getColumn(columnIndex));
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex >= 0)
        {
            group = columnIndex / FakeColumn.NUMBER_OF_COLUMNS.idx;
            setValueFor(aValue, rowIndex, FakeColumn.getColumn(columnIndex % FakeColumn.NUMBER_OF_COLUMNS.idx));
            return;
        }
        setValueFor(aValue, rowIndex, FakeColumn.getColumn(columnIndex));
    }

    @Override
    public Object getValueFor(int entryIdx, FakeColumn property)
    {
        FakeEntry entry = getData().get(entryIdx);

        if (property.idx >= 0)
        {
            // a cell past the end of this entry's list simply has nothing in it. Filling the
            // list in so that there is something to return would make looking at the sheet
            // change the file it is showing.
            if (currentGroup() >= entry.getRepeated().size())
                return null;
            return entry.getRepeated().get(currentGroup())[property.idx];
        }

        if (property == FakeColumn.ID)
            return entryIdx;

        if (property == FakeColumn.NAME)
        {
            TextBankData names = getNameTextBank();
            return entryIdx < names.size() ? names.get(entryIdx).getText() : "";
        }

        return null;
    }

    @Override
    public void setValueFor(Object aValue, int entryIdx, FakeColumn property)
    {
        FakeEntry entry = getData().get(entryIdx);
        aValue = prepareObjectForWriting(aValue, property.cellType);

        if (property.idx >= 0)
        {
            int value = (Integer) aValue;
            int[] range = property.getValueRange();
            // the range this column advertises to the cell editors is the one the write path
            // enforces, so a value which arrives by some other route (a paste, say) cannot be
            // stored only to be truncated when the file is written back out
            if (value < range[0] || value > range[1])
                throw new IllegalArgumentException(property + " accepts " + range[0] + ".." + range[1] + ", got " + value);

            while (currentGroup() >= entry.getRepeated().size())
                entry.getRepeated().add(new int[FakeEntry.GROUP_WIDTH]);

            entry.getRepeated().get(currentGroup())[property.idx] = value;
            return;
        }

        if (property == FakeColumn.NAME)
            getNameTextBank().get(entryIdx).setText(String.valueOf(aValue));
    }

    @Override
    protected CellTypes getCellType(int columnIndex)
    {
        if (columnIndex >= 0)
            return FakeColumn.getColumn(columnIndex % FakeColumn.NUMBER_OF_COLUMNS.idx).cellType;
        return FakeColumn.getColumn(columnIndex).cellType;
    }

    @Override
    public int[] getCellValueRange(int columnIndex)
    {
        if (columnIndex >= 0)
            return FakeColumn.getColumn(columnIndex % FakeColumn.NUMBER_OF_COLUMNS.idx).getValueRange();
        return FakeColumn.getColumn(columnIndex).getValueRange();
    }

    @Override
    public TextBankData getNameTextBank()
    {
        return getTextBankData().get(0);
    }

    @Override
    public FormatModel<FakeEntry, FakeColumn> getFrozenColumnModel()
    {
        return new FakeModel(getData(), getTextBankData()) {
            @Override
            public int getColumnCount()
            {
                return super.getNumFrozenColumns();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                return super.getValueAt(rowIndex, columnIndex - super.getNumFrozenColumns());
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex)
            {
                super.setValueAt(aValue, rowIndex, columnIndex - super.getNumFrozenColumns());
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };
    }

    /** convenience for tests: a sheet of {@code rowCount} entries, each already fully populated */
    public static List<FakeEntry> populatedEntries(int rowCount)
    {
        List<FakeEntry> entries = new ArrayList<>();
        for (int row = 0; row < rowCount; row++)
            entries.add(new FakeEntry(row, row * 2).withGroups(MAX_GROUPS));
        return entries;
    }

    /** convenience for tests: a sheet of {@code rowCount} entries which hold no groups at all */
    public static List<FakeEntry> emptyEntries(int rowCount)
    {
        List<FakeEntry> entries = new ArrayList<>();
        for (int row = 0; row < rowCount; row++)
            entries.add(new FakeEntry(row, row * 2));
        return entries;
    }

    public enum FakeColumn
    {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        FIRST(0, "move", CellTypes.COMBO_BOX),
        SECOND(1, "level", CellTypes.INTEGER),
        NUMBER_OF_COLUMNS(2, null, null);

        final int idx;
        final String key;
        final CellTypes cellType;

        FakeColumn(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        int[] getValueRange()
        {
            return switch (this) {
                case FIRST -> new int[] {0, 511};
                case SECOND -> new int[] {0, 127};
                default -> new int[] {0, 0xFFFF};
            };
        }

        static FakeColumn getColumn(int idx)
        {
            for (FakeColumn column : values())
            {
                if (column.idx == idx)
                    return column;
            }
            return NUMBER_OF_COLUMNS;
        }
    }
}
